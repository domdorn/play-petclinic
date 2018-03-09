package petclinic.api.specialty.getAllSpecialties

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._

class GetAllSpecialtiesController @Inject()(specialtyService: GetAllSpecialtiesService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val ec = cc.executionContext

  def getAll: Action[AnyContent] = cc.actionBuilder.async { req =>
    specialtyService.getAll().map(x => Results.Ok(Json.toJson(x)))
  }

}
