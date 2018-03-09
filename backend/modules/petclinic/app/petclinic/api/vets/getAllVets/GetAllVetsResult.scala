package petclinic.api.vets.getAllVets

import play.api.libs.json.Json

private[getAllVets] case class GetAllVetsApiResult (data: Seq[Vet])
private[getAllVets] case class Vet(id: String, firstName: String, lastName: String)


object GetAllVetsApiResult {
  implicit val petFormat = Json.format[Vet]
  implicit val format = Json.format[GetAllVetsApiResult]
}