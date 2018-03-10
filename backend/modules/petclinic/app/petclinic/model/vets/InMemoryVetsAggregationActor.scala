package petclinic.model.vets

import java.time.Instant

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, ReceiveTimeout, Stash}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.stream.{ActorMaterializer, KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import petclinic.model.vets.Protocol.{BasicVet, GetAllVets, GetAllVetsReponse}
import petclinic.model.vets.Vet.Events._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object InMemoryVetsAggregationActor {
  val SELECT: Sink[String, Future[Seq[String]]] = Sink.seq[String]
  def FROM_INITIAL(readJournal: CassandraReadJournal) = readJournal.currentPersistenceIds()
  def FROM_CONTINOUSLY(readJournal: CassandraReadJournal) = readJournal.persistenceIds()
  val WHERE = Flow[String].filter(s => s.startsWith("vet-"))

  def startInitialImport(readJournal: CassandraReadJournal, recipient: ActorRef)(implicit actorSystem: ActorSystem) = FROM_INITIAL(readJournal).via(WHERE)
    .flatMapConcat(id => readJournal.currentEventsByPersistenceId(id,0, Integer.MAX_VALUE))
    .runForeach(ev => recipient ! ev)(ActorMaterializer()) // send all the events to ourself

  def startContinuousImport(readJournal: CassandraReadJournal, recipient: ActorRef, offsetMap: scala.collection.immutable.Map[String, Long])(implicit actorSystem: ActorSystem): UniqueKillSwitch = FROM_CONTINOUSLY(readJournal)
    .via(WHERE).alsoTo(Sink.foreach(id => println(s"in continous import, handling id $id"))).flatMapMerge(20, id => readJournal.eventsByPersistenceId(id, offsetMap.getOrElse(id, 0), Long.MaxValue))
    .viaMat(KillSwitches.single)(Keep.right)
    .alsoTo(Sink.foreach(ev => println(s"in continuous import, handling event: " + ev)))
    .toMat(Sink.foreach { ev: EventEnvelope => recipient ! ev })(Keep.left)
    .run()(ActorMaterializer())

}

class InMemoryVetsAggregationActor extends Actor with Stash {
  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val actorSystem: ActorSystem = context.system
  import InMemoryVetsAggregationActor._

  val readJournal: CassandraReadJournal = PersistenceQuery(context.system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  var continuousImport: Option[UniqueKillSwitch] = None
  var lastOffset: Map[String, Long] = Map()

  private val ImportingDone = "Done importing"
  akka.pattern.Patterns.pipe(startInitialImport(readJournal, self).map(_ => {
    println("initial importing is done...")
    ImportingDone
  }), context.dispatcher).to(self)

  var readyToServe = false
  var vets: Map[String,BasicVet] = Map.empty

  context.setReceiveTimeout(30 seconds)

  override def receive: Receive = {
    case eev@EventEnvelope(offset, persistenceId, sequenceNr, ev) => ev.asInstanceOf[Vet.Events.Event] match {
      case e@VetCreatedEvent(id, firstName, lastName, specialties) => {
        println("InMemoryVetsAggregationActor received VetCreatedEvent: " + e + " at " + Instant.now())
        lastOffset = lastOffset.updated(persistenceId, eev.sequenceNr)
        vets = vets + (persistenceId -> BasicVet(id, firstName, lastName))
      }
      case SpecialtyAddedEvent(specialtyId) => // ignore
      case SpecialtyRemovedEvent(specialtyId) => // ignore
      case FirstNameChangedEvent(firstNameIn) => vets = vets.updated(persistenceId, vets(persistenceId).copy(firstName = firstNameIn))
      case LastNameChangedEvent(lastName) => vets = vets.updated(persistenceId, vets(persistenceId).copy(lastName = lastName))
    }
    case `ImportingDone` => {
      readyToServe = true
      unstashAll()
//      println("InMemoryVetsAggregationActor finished importing")
      continuousImport = Some(startContinuousImport(readJournal, self, lastOffset))
//      println("Continuous Import started: " + continuousImport)
    }
    case GetAllVets => {
//      println("request to get all vets received")
      if (!readyToServe) {
//        println("stashing it away, as we're not ready to serve")
        stash()
      } else {
        sender() ! GetAllVetsReponse(vets.values.toSeq)
      }
    }
    case ReceiveTimeout => {
      println("shutting down InMemoryVetsAggregationActor")
      continuousImport.foreach(ks => {
        println("stopping continuous import")
        ks.shutdown()
      })
      context.stop(self)
    }
  }
}
