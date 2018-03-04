package petclinic.api.pettype.changePetTypeName

import javax.inject.Inject

import petclinic.api.model.{PetType, PetTypeJsonSupport}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

class ChangePetTypeNameController @Inject()(petTypeService: ChangePetTypeNameService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def changePetTypeName(id: String) = cc.actionBuilder.async(cc.parsers.json) { req =>

    val petType = req.body.as[PetType]
    petTypeService.changeName(id, petType.getName).toScala.map(_.asScala).map {
      case Some(pt) => Results.NoContent
      case None => Results.BadRequest
    }.recover {
      case x: IllegalArgumentException => {
        val errorData = Seq(
          "status" -> "400",
          "statusText" -> "bad request",
          "message" -> "name in use or invalid"
        ).toMap
        Results.BadRequest(Json.toJson(errorData))

      }
      case x => {
        val errorData: Map[String, String] = Seq(
          "status" -> "500",
          "statusText" -> "internal server error happened",
          "message" -> "an error occured"
        ).toMap
        Results.InternalServerError(Json.toJson(errorData))
      }
    }

  }

}
