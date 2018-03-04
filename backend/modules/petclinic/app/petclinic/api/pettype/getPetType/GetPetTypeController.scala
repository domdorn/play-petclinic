package petclinic.api.pettype.getPetType

import javax.inject.Inject

import petclinic.api.model.PetTypeJsonSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

class GetPetTypeController @Inject()(petTypeService: GetPetTypeService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext


  def getPetType(id: String) = cc.actionBuilder.async {
    req =>
      petTypeService.getPetType(id).toScala.map(_.asScala).map {
        case Some(pt) => Results.Ok(Json.toJson(pt))
        case None => Results.NotFound
      }
  }

}
