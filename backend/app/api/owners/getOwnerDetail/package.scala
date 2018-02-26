package api.owners.getOwnerDetail

import java.time.LocalDate
import java.util

import scala.beans.BeanProperty

// object received when just querying one owner
case class OwnerGetDetailedPetOwner(@BeanProperty id: String)
case class OwnerGetDetailedPet(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty birthDate: LocalDate, @BeanProperty `type`: String, @BeanProperty owner: OwnerGetDetailedPetOwner)

case class OwnerGetDetailApiResponse(@BeanProperty id: String, @BeanProperty firstName: String, @BeanProperty lastName: String, @BeanProperty address: String, @BeanProperty city: String, @BeanProperty telephone: String, @BeanProperty pets: util.Collection[OwnerGetDetailedPet])

