package petclinic.api.specialty.deleteSpecialty

import javax.inject.Inject
import play.api.mvc._

class DeleteSpecialtyController @Inject()(cc: ControllerComponents, service: DeleteSpecialtyService) extends AbstractController(cc) {

  implicit val ec = cc.executionContext

  def delete(id: String): Action[AnyContent] = cc.actionBuilder.async { req =>
    service.delete(id).map {
      case Some(worked) => if (worked) Results.NoContent else Results.Forbidden("specialty still in use")
      case None => Results.BadRequest("error")
    }
  }

}
