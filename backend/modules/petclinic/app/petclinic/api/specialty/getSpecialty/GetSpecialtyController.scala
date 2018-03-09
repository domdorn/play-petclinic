package petclinic.api.specialty.getSpecialty

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._

class GetSpecialtyController @Inject()(specialtyService: GetSpecialtyService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val ec = cc.executionContext


  def get(id: String) = cc.actionBuilder.async {
    req =>
      specialtyService.getSpecialty(id).map {
        case Some(pt) => Results.Ok(Json.toJson(pt))
        case None => Results.NotFound
      }
  }

}
