package petclinic.api.vets.changeVet

import javax.inject.Inject
import petclinic.api.model.{PetType, PetTypeJsonSupport}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

case class ChangeVetApiSpecialty(id: Int)
case class ChangeVetApiRequest(id: String, firstName: String, lastName: String, specialties: Seq[ChangeVetApiSpecialty])


class ChangeVetController @Inject()(service: ChangeVetService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext
  implicit val changeVetApiSpecialtyReads = Json.reads[ChangeVetApiSpecialty]
  implicit val changeVetApiRequestReads = Json.reads[ChangeVetApiRequest]

  def change(id: String) = cc.actionBuilder.async(cc.parsers.json[ChangeVetApiRequest]) { req =>

    val data = req.body
    service.change(id, data).map {
      case Some(true) => Results.NoContent
      case Some(false) => Results.BadRequest
      case None => Results.NotFound("no vet with this id found")
    }.recover {
      case x: IllegalArgumentException =>
        val errorData = Seq(
          "status" -> "400",
          "statusText" -> "bad request",
          "message" -> "failed to update vet"
        ).toMap
        Results.BadRequest(Json.toJson(errorData))
      case x =>
        val errorData: Map[String, String] = Seq(
          "status" -> "500",
          "statusText" -> "internal server error happened",
          "message" -> "an error occured",
          "detail" -> x.getMessage
        ).toMap
        Results.InternalServerError(Json.toJson(errorData))
    }

  }

}
