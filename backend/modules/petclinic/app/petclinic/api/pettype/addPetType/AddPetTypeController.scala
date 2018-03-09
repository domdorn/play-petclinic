package petclinic.api.pettype.addPetType

import javax.inject.Inject

import petclinic.api.model.{PetType, PetTypeJsonSupport}
import play.api.libs.json.Json
import play.api.mvc._

import scala.compat.java8.FutureConverters._

class AddPetTypeController @Inject()(petTypeService: AddPetTypeService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def add = cc.actionBuilder.async(cc.parsers.json[PetType]) { req =>
    val petType = req.body
    petTypeService.create(petType.getName).map(res => Results.Ok(Json.toJson(res)))
  }

}
