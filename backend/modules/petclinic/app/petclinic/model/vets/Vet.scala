package petclinic.model.vets

import java.time.Instant

import akka.actor.{Props, ReceiveTimeout}
import akka.persistence.PersistentActor
import petclinic.model.vets.Protocol._


object Vet {

  object Events {
    sealed trait Event
    case class VetCreatedEvent(id: String, firstName: String, lastName: String, specialties: Seq[String]) extends Event
    case class SpecialtyAddedEvent(specialtyId: String) extends Event
    case class SpecialtyRemovedEvent(specialtyId: String) extends Event

    case class FirstNameChangedEvent(firstName: String) extends Event
    case class LastNameChangedEvent(lastName: String) extends Event

    // event that gets distributed to the event-bus
    case class VetUpdate(id: String, events: Seq[Event])
  }

  case class VetData(
                      firstName: String, lastName: String,
                      telephone: String, address: String,
                      specialties: Seq[String]
                    )

  def props(id: String) = Props(new Vet(id))
}


class Vet(id: String) extends PersistentActor {

  import Vet.Events._
  import Vet._
  import scala.concurrent.duration._

  override def persistenceId: String = s"vet-$id"

  context.setReceiveTimeout(3 seconds)

  var state: Option[VetData] = None

  override def receiveRecover: Receive = {
    case x: Vet.Events.Event => x match {
      case VetCreatedEvent(_, firstName, lastName, specialties) => state = Some(VetData(
        firstName, lastName, "", "", Nil))
      case SpecialtyAddedEvent(specialtyId) => state = state
        .map(s => s.copy(specialties = s.specialties ++ Seq(specialtyId)))
      case SpecialtyRemovedEvent(specialtyId) => state = state
        .map(s => s.copy(specialties = s.specialties.filterNot(_ == specialtyId)))
      case FirstNameChangedEvent(firstName) => state = state.map(vd => vd.copy(firstName = firstName))
      case LastNameChangedEvent(lastName) => state = state.map(vd => vd.copy(lastName = lastName))
    }
  }

  override def receiveCommand: Receive = {
    case x: Protocol.CreateVetCommand => persist(Events.VetCreatedEvent(id, x.firstname, x.lastName, x.specialties)) {
      ev =>
        receiveRecover.apply(ev)
        sender() ! VetCreatedResponse(ev.id, ev.firstName, ev.lastName, Nil)
        println("Created " + ev + " in Vet at " + Instant.now())
        context.system.eventStream.publish(VetUpdate(ev.id, Seq(ev)))
    }
    case x: GetVetDetailsQuery =>
      state match {
        case Some(s) => sender() ! VetDetails(id, s.firstName, s.lastName, s.specialties)
        case None =>
          sender() ! GetDetailsVetNotFound
          context.stop(self)
      }
    case x: UpdateVetCommand => state match {
      case None =>
        sender() ! VetToUpdateNotFound
        context.stop(self)
      case Some(currentState) =>
        val eventsToCreate : scala.collection.immutable.Seq[Event] = scala.collection.immutable.Seq.empty[Event] ++
         (if(currentState.firstName != x.firstName) { Seq(FirstNameChangedEvent(x.firstName))} else Seq.empty) ++
         (if(currentState.lastName != x.lastName) { Seq(LastNameChangedEvent(x.lastName))} else Seq.empty) ++
         {
          val addedSpecialties = x.specialties.filterNot(s => currentState.specialties.contains(s))
          val removedSpecialties = currentState.specialties.filterNot(s => x.specialties.contains(s))

          Seq.empty[Event] ++ addedSpecialties.map(s => SpecialtyAddedEvent(s)) ++ removedSpecialties.map(s => SpecialtyRemovedEvent(s))
        }

        persistAll[Event](eventsToCreate){ event =>
          receiveRecover.apply(event)
        }
        context.system.eventStream.publish(VetUpdate(id, eventsToCreate))
        sender() ! VetUpdatedResponse
    }
    case ReceiveTimeout => context.stop(self)
  }
}
