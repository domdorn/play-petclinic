package petclinic.api.owners.createOwner

import play.api.libs.json.Json

case class OwnerCreate(firstName: String, lastName: String, city: String, telephone: String, address: String)

object OwnerCreate {
  implicit val format = Json.format[OwnerCreate]
}
