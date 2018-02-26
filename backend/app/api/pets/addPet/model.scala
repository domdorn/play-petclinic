package api.pets.addPet

import play.api.libs.json.Json

case class AddPetApiResponse(id: String)

object AddPetApiResponse {
  implicit val format = Json.format[AddPetApiResponse]
}

