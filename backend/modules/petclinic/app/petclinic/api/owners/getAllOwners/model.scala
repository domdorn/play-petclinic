package petclinic.api.owners.getAllOwners

import java.util

import play.api.libs.json.Json

// objects received when querying all owners
case class OwnerGetPet(name: String)
case class OwnerGetApiResponse(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[OwnerGetPet])

private[getAllOwners] trait JsonSupport {
  implicit val OwnerGetPetFormat = Json.format[OwnerGetPet]
  implicit val OwnerGetApiResponseFormat = Json.format[OwnerGetApiResponse]
}