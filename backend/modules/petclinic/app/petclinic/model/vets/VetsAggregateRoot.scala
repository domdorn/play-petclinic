package petclinic.model.vets

import java.util.UUID

import akka.actor.{ActorRef, Props, Terminated}
import akka.persistence.PersistentActor
import javax.inject.Inject
import petclinic.model.vets.Protocol._
import petclinic.model.vets.getAllAggregation.{AskAllAggregationActor, InMemoryVetsAggregationActorWithReadJournal}
import play.api.Configuration


object VetsAggregateRoot {

  object Events {

    sealed trait Event

    case class VetCreatedEvent(id: String, firstName: String, lastName: String, specialties: Seq[String]) extends Event

    case class VetDeletedEvent(id: String) extends Event

  }

  case class VetAggregateStateData(id: String, firstName: String, lastName: String, specialties: Seq[String] = Nil)

}

class VetsAggregateRoot @Inject() (configuration: Configuration) extends PersistentActor {

  import VetsAggregateRoot.Events._
  import VetsAggregateRoot._

  override def persistenceId: String = "vets-aggregate"

  var vets: Vector[VetAggregateStateData] = Vector.empty // what If I have 1.000.000 vets?!

  var aggregationActor: Option[ActorRef] = None

  override def receiveRecover: Receive = {
    case x: Event => x match {
      case VetsAggregateRoot.Events.VetCreatedEvent(id, firstName, lastName, specialties) => vets = vets ++ Seq(VetAggregateStateData(id, firstName, lastName, specialties))
      case VetsAggregateRoot.Events.VetDeletedEvent(id) => vets = vets.filterNot(_.id == id)
    }
  }

  override def receiveCommand: Receive = {
    case x: Command => handleCommands(x)
    case x: Query => handleQueries(x)
    case Terminated(ref) => aggregationActor.map(_ == ref).foreach(bool => if(bool){
      aggregationActor = None
    })
  }

  def handleCommands = (x: Command) => x match {
    case cmd@CreateVetCommand(firstName, lastName, specialties) => vets.find(v => v.firstName == firstName && v.lastName == lastName) match {
      case Some(value) => sender() ! VetCouldNotBeCreatedResponse
      case None => persist(
        VetsAggregateRoot.Events.VetCreatedEvent(UUID.randomUUID().toString, firstName, lastName, specialties)
      ) { ev =>
        println("persisted event")
        receiveRecover.apply(ev)
        println("telling child")
        getOrCreateChild(ev.id).forward(cmd)
        }
    }
    case x@UpdateVetCommand(id, firstName, lastName, specialties) => getOrCreateChild(id) forward(x)
  }

  def handleQueries = (x: Query) => x match {
    case GetAllVets => {
      if (aggregationActor.isEmpty) { // lazy initialize
        val ref = createAggregationActor()
        context.watch(ref)
        aggregationActor = Some(ref)
      }
      aggregationActor.foreach(_.forward(GetAllVets))
    }
    case msg@GetVetDetailsQuery(id) => getOrCreateChild(id).forward(msg)
  }

  def getOrCreateChild(id: String): ActorRef = context.child(id).getOrElse(context.actorOf(Vet.props(id), id))

  def createAggregationActor() : ActorRef = {
    val vetsAggregate: String = configuration.get[String]("com.dominikdorn.petclinic.vets.aggregate")

    vetsAggregate match {
      case "AskAll" =>  context.actorOf(AskAllAggregationActor.props(vets.map(vad => vad.id), self))
      case "AskAllFuture" => ???
      case "InMemoryWithReadJournalUpdate" => context.actorOf(Props(new InMemoryVetsAggregationActorWithReadJournal()))
      case "InMemoryWithEventsUpdate" => ???
      case _ => throw new IllegalStateException("Unmatched aggregation actor type")
    }

  }

}



