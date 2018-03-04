package petclinic.api.pettype.getAllPetTypes

import javax.inject.Inject

import petclinic.api.model.PetTypeJsonSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

class GetAllPetTypesController @Inject()(petTypeService: GetAllPetTypeService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def getAllPetTypes(): Action[AnyContent] = cc.actionBuilder.async {
    req =>
      petTypeService.getAll.toScala.map(types => types.asScala.toSeq).map(x => Results.Ok(Json.toJson(x)))
  }

}
