package petclinic.api.pettype.deletePetType

import javax.inject.Inject
import petclinic.api.model.PetTypeJsonSupport
import play.api.mvc._

class DeletePetTypeController @Inject()(cc: ControllerComponents, service: DeletePetTypeService) extends AbstractController(cc) with PetTypeJsonSupport {

  implicit val ec = cc.executionContext

  def delete(id: String): Action[AnyContent] = cc.actionBuilder.async { req =>
    service.deleteType(id).map {
      case Some(worked) => if (worked) Results.NoContent else Results.Forbidden("type still in use")
      case None => Results.BadRequest("error")
    }
  }

}
