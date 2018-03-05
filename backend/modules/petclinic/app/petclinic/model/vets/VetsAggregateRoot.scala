package petclinic.model.vets

import java.util.UUID

import akka.persistence.PersistentActor
import petclinic.model.vets.Protocol._
import petclinic.model.vets.VetEvents.{SpecialtyAddedEvent, SpecialtyRemovedEvent, VetCreatedEvent}


object VetsAggregateRoot {

  sealed trait Event

  case class VetCreatedEvent(id: String, name: String) extends Event

  case class VetDeletedEvent(id: String) extends Event

}

class VetsAggregateRoot extends PersistentActor {

  import VetsAggregateRoot._

  override def persistenceId: String = "vets-aggregate"

  case class Vet(id: String, name: String,
                 telephone: String, address: String,
                 specialties: Seq[String]
                )

  var vets: Seq[Vet] = Seq() // what If I have 1.000.000 vets?!


  override def receiveRecover: Receive = {
    case x: Event => x match {
      case VetCreatedEvent(id, name) => vets = vets ++ Seq(Vet(id, name))
      case VetDeletedEvent(id) => vets = vets.filterNot(_.id == id)
    }
  }

  override def receiveCommand: Receive = {
    case x: Command => handleCommands(x)
    case x: Query => handleQueries(x)
  }

  def handleCommands = (x: Command) => x match {
    case CreateVetCommand(name) => vets.find(_.name == name) match {
      case Some(value) => sender() ! VetCouldNotBeCreatedResponse
      case None => persist(VetCreatedEvent(UUID.randomUUID().toString, name)) { ev =>
        receiveRecover.apply(ev)
        sender() ! VetCreatedResponse(ev.id)
      }
    }
    case UpdateVetCommand(id, name) =>
  }

  def handleQueries = (x: Query) => x match {
    case GetAllVets =>
    case GetVetDetailsQuery(id) =>
  }

}

  object VetEvents {
    sealed trait Event
    case class VetCreatedEvent(id: String, firstName: String, lastName: String, address: String, telephone: String) extends Event
    case class SpecialtyAddedEvent(specialtyId: String) extends Event
    case class SpecialtyRemovedEvent(specialtyId: String) extends Event
  }

  class Vet(id: String) extends PersistentActor {
    override def persistenceId: String = s"vet-$id"

    case class VetData(
                        firstName: String, lastName: String,
                        telephone: String, address: String,
                        specialties: Seq[String]
                      )

    var state: Option[VetData] = None

    override def receiveRecover: Receive = {
      case x: VetEvents.Event => x match {
        case VetCreatedEvent(_, firstName, lastName, address, telephone) => state = Some(VetData(
          firstName, lastName, telephone, address, Nil))
        case SpecialtyAddedEvent(specialtyId) => state = state
          .map(s => s.copy(specialties = s.specialties ++ Seq(specialtyId)))
        case SpecialtyRemovedEvent(specialtyId) => state = state
          .map(s => s.copy(specialties = s.specialties.filterNot(_ == specialtyId)))
      }
    }

    override def receiveCommand: Receive = ???
  }


