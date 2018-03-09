package petclinic.model.owners.owner

import java.time.LocalDate

object Events {
  sealed trait Event

  case class OwnerCreatedEvent(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String) extends Event
  case class PetAddedEvent(id: String, name: String, birthDay: LocalDate, petTypeId: String) extends Event

  sealed trait PetChangedEvent extends Event {
    val id: String
  }
  case class PetNameChangedEvent(id: String, name: String) extends PetChangedEvent
  case class PetBirthdayChangedEvent(id: String, birthDay: LocalDate) extends PetChangedEvent
  case class PetTypeChangedEvent(id: String, newTypeId: String) extends PetChangedEvent
}
