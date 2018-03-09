package petclinic.api.pettype.getAllPetTypes

import javax.inject.Inject
import petclinic.api.model.PetTypeJsonSupport
import play.api.libs.json.Json
import play.api.mvc._

class GetAllPetTypesController @Inject()(petTypeService: GetAllPetTypeService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def getAll: Action[AnyContent] = cc.actionBuilder.async { req =>
    petTypeService.getAll().map(x => Results.Ok(Json.toJson(x)))
  }

}
