package petclinic.model.vets

import java.time.Instant

import akka.actor.Props
import akka.persistence.PersistentActor
import petclinic.model.vets.Protocol.VetCreatedResponse



object Vet {
  object Events {
    sealed trait Event
    case class VetCreatedEvent(id: String, firstName: String, lastName: String, specialties: Seq[String]) extends Event
    case class SpecialtyAddedEvent(specialtyId: String) extends Event
    case class SpecialtyRemovedEvent(specialtyId: String) extends Event
  }
  case class VetData(
                      firstName: String, lastName: String,
                      telephone: String, address: String,
                      specialties: Seq[String]
                    )

  def props(id: String) = Props(new Vet(id))
}


class Vet(id: String) extends PersistentActor {
  import Vet._
  import Vet.Events._
  override def persistenceId: String = s"vet-$id"


  var state: Option[VetData] = None

  override def receiveRecover: Receive = {
    case x: Vet.Events.Event => x match {
      case VetCreatedEvent(_, firstName, lastName, specialties) => state = Some(VetData(
        firstName, lastName, "", "", Nil))
      case SpecialtyAddedEvent(specialtyId) => state = state
        .map(s => s.copy(specialties = s.specialties ++ Seq(specialtyId)))
      case SpecialtyRemovedEvent(specialtyId) => state = state
        .map(s => s.copy(specialties = s.specialties.filterNot(_ == specialtyId)))
    }
  }

  override def receiveCommand: Receive = {
    case x: Protocol.CreateVetCommand => persist(Events.VetCreatedEvent(id, x.firstname, x.lastName, x.specialties)) {
      ev => receiveRecover.apply(ev)
        sender() ! VetCreatedResponse(ev.id, ev.firstName, ev.lastName, Nil)
        println("Created " + ev + " in Vet at " + Instant.now())
    }
  }
}
