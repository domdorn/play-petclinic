package petclinic.model.owners

import java.time.LocalDate

import akka.actor.Props
import akka.actor.Status.Failure
import akka.persistence.PersistentActor

object Owner {

  def props(uuid: String) = Props(classOf[Owner], uuid)

  case class Pet(id: String, name: String, birthDay: LocalDate, `type`: String)

  case class State(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[Pet]) {
    def updatePet(newPet: Pet): State = {
      copy(pets = pets.filterNot(_.id == newPet.id) ++ Seq(newPet))
    }
  }

  case object GetOverviewState

}

class Owner(uuid: String) extends PersistentActor {

  import owner.Events._
  import owner.Protocol._

  override def persistenceId: String = s"owner-$uuid"

  var state: Owner.State = _
  var lastPetId = 0

  override def receiveRecover: Receive = {
    case x: OwnerCreatedEvent => state = Owner.State(x.id, x.firstName, x.lastName, x.address, x.city, x.telephone, Nil)

    case x: PetAddedEvent => {
      state = state.copy(pets = state.pets ++ Seq(Owner.Pet(x.id, x.name, x.birthDay, x.petTypeId)))
      lastPetId = lastPetId + 1
    }

    case x: PetChangedEvent => {
      val pet = state.pets.find(_.id == x.id)
      pet match {
        case None => throw new IllegalStateException("this may not happen")
        case Some(p) => x match {
          case PetNameChangedEvent(id, name) => state = state.updatePet(p.copy(name = name))
          case PetBirthdayChangedEvent(id, birthDay) => state = state.updatePet(p.copy(birthDay = birthDay))
          case PetTypeChangedEvent(id, newTypeId) => state = state.updatePet(p.copy(`type` = newTypeId))
        }
      }

    }
  }

  override def receiveCommand: Receive = {

    case x: CreateOwnerCommand => {
      persist(OwnerCreatedEvent(uuid, x.firstName, x.lastName, x.address, x.city, x.telephone)) { ev =>
        receiveRecover.apply(ev)
      }
      sender ! uuid
    }
    case x: AddPetCommand if x.ownerId == uuid => {
      state.pets.find(p => p.name == x.name && p.`type` == x.petTypeId && p.birthDay == x.birthday) match {
        case Some(pet) => sender() ! PetAddedResponse(pet.id, pet.name, pet.birthDay, pet.`type`)
        case None => persist(PetAddedEvent((lastPetId + 1).toString, x.name, x.birthday, x.petTypeId)) { ev =>
          receiveRecover(ev)
          sender() ! PetAddedResponse(ev.id, ev.name, ev.birthDay, ev.petTypeId)
        }
      }
    }

    case x: UpdatePetDetailsCommand => state.pets.find(_.id == x.petId) match {
      case None => sender() ! PetNotFoundUpdatePetDetailsResponse
      case Some(pet) => {
        val empty = scala.collection.immutable.Nil

        val changeEvents: scala.collection.immutable.Seq[PetChangedEvent] = empty ++
          (if (pet.name != x.name) Seq(PetNameChangedEvent(pet.id, x.name)) else empty) ++
          (if (!pet.birthDay.isEqual(x.birthday)) Seq(PetBirthdayChangedEvent(pet.id, x.birthday)) else empty) ++
          (if (pet.`type` != x.petType.getId) Seq(PetTypeChangedEvent(pet.id, x.petType.getId)) else empty)


        persistAll[PetChangedEvent](changeEvents) {
          event =>
            receiveRecover(event)
        }
        sender() ! PetDetailsUpdatedResponse(pet.id)
      }
    }

    case OverviewStateQuery => sender ! OwnerOverviewStateResponse(state.id, state.firstName, state.lastName, state.address, state.city, state.telephone, state.pets.map(_.name))
    case GetOwnerDetailsQuery(id) if id == uuid => sender() ! GetOwnerDetailsResponse(uuid, state.firstName, state.lastName, state.address, state.city, state.telephone, state.pets)
    case GetOwnerDetailsQuery(id) => Failure(new IllegalArgumentException("should not have received this id"))

    case x@GetPetDetailsQuery(ownerId, petId) => {
      state.pets.find(_.id == petId) match {
        case Some(pet) => sender() ! PetDetailsResponse(ownerId: String, state.firstName, state.lastName, pet.id, pet.name, pet.birthDay, pet.`type`)
        case None => sender() ! PetNotFoundGetPetDetailsresponse
      }
    }

  }

}