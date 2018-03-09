package petclinic.api.specialty.changeSpecialtyName

import javax.inject.Inject
import petclinic.api.model.Specialty
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

class ChangeSpecialtyNameController @Inject()(SpecialtyService: ChangeSpecialtyNameService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val ec = cc.executionContext

  def change(id: String) = cc.actionBuilder.async(cc.parsers.json) { req =>

    val specialty = req.body.as[Specialty]
    SpecialtyService.changeName(id, specialty.name).map {
      case Some(pt) => Results.NoContent
      case None => Results.BadRequest
    }.recover {
      case x: IllegalArgumentException =>
        val errorData = Seq(
          "status" -> "400",
          "statusText" -> "bad request",
          "message" -> "name in use or invalid"
        ).toMap
        Results.BadRequest(Json.toJson(errorData))
      case x =>
        val errorData: Map[String, String] = Seq(
          "status" -> "500",
          "statusText" -> "internal server error happened",
          "message" -> "an error occured"
        ).toMap
        Results.InternalServerError(Json.toJson(errorData))
    }

  }

}
