package petclinic.api.owners.getOwnerDetail

import java.time.LocalDate
import java.util

import play.api.libs.json.Json

// object received when just querying one owner
case class OwnerGetDetailedPetOwner(id: String)
case class OwnerGetDetailedPet(id: String, name: String, birthDate: LocalDate, `type`: String, owner: OwnerGetDetailedPetOwner)
case class OwnerGetDetailApiResponse(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[OwnerGetDetailedPet])

private[getOwnerDetail] trait JsonSupport {
  implicit val ownerGetDetailedPetOwnerFormat = Json.format[OwnerGetDetailedPetOwner]
  implicit val OwnerGetDetailedPetFormat = Json.format[OwnerGetDetailedPet]
  implicit val OwnerGetDetailApiResponseFormat = Json.format[OwnerGetDetailApiResponse]
}
