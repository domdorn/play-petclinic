package petclinic.model.vets.getAllAggregation

import akka.actor.{Actor, ActorRef, Cancellable, Props, ReceiveTimeout}
import petclinic.model.vets.Protocol._
import petclinic.model.vets.Vet

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object AskAllAggregationActor {

  def props(children: Seq[String], aggregate: ActorRef) = Props(new AskAllAggregationActor(children, aggregate))
}

class AskAllAggregationActor(children: Seq[String], aggregate: ActorRef) extends Actor {
  println("creating AskAllAggregationActor ")
  implicit val ec: ExecutionContext = context.dispatcher

  context.system.eventStream.subscribe(self, classOf[Vet.Events.VetUpdate])

  children.foreach(childId => aggregate ! GetVetDetailsQuery(childId))

  context.setReceiveTimeout(30 seconds)
  var cancellable: Cancellable = context.system.scheduler.scheduleOnce(1 second, self, "shouldBeDone")

  var askers: Vector[ActorRef] = Vector.empty
  var answersMissing: Seq[String] = children
  var readyToServe: Boolean = answersMissing.isEmpty
  var vets: Map[String, BasicVet] = Map.empty
  notifyAskersIfNecessary() // if we don't have any vets in the system, we're sending an empty response

  override def receive: Receive = {

    // if we receive something from the eventstream, ask for the new data
    case petclinic.model.vets.Vet.Events.VetUpdate(vetId, x) => {
      println("received data through the event stream from sender :" + sender())
      aggregate ! GetVetDetailsQuery(vetId)
    }

    case "shouldBeDone" => {
      println("Warning, didn't receive answers in due time")
      val response = GetAllVetsReponse(vets.values.toSeq)
      askers.foreach(ar => ar ! response)
      askers = Vector.empty
      readyToServe = true
    }

    case VetDetails(id, firstName, lastName, specialties) => {
      vets = vets + (id -> BasicVet(id, firstName, lastName))
      answersMissing = answersMissing.filterNot(i => i == id)
      notifyAskersIfNecessary()
    }

    case GetAllVets => {
      askers = askers :+ sender()
      if (readyToServe) {
        askers.foreach(asker => asker ! GetAllVetsReponse(vets.values.toSeq))
      }
    }

    case ReceiveTimeout => {
      println("shutting down AskAllAggregationActor")
      context.stop(self)
    }
  }

  def notifyAskersIfNecessary(): Unit = {
    if (answersMissing.isEmpty) {
      cancellable.cancel()
      readyToServe = true
      val response = GetAllVetsReponse(vets.values.toSeq)
      askers.foreach(ar => ar ! response)
      askers = Vector.empty
    }

  }
}
