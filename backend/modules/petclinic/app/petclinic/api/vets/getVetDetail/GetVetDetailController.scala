package petclinic.api.vets.getVetDetail

import javax.inject.Inject
import petclinic.api.model._
import play.api.libs.json._
import play.api.mvc._

class GetVetDetailController @Inject()(service: GetVetDetailService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext
  implicit val GetVetDetailSpecialtyWrites: OWrites[GetVetDetailSpecialty] = Json.writes[GetVetDetailSpecialty]
  implicit val GetVetDetailResponseWrites: OWrites[GetVetDetailApiResponse] = Json.writes[GetVetDetailApiResponse]

  def get(vetId: String): Action[AnyContent] = cc.actionBuilder.async { req =>
    service.getVetDetail(vetId).map {
      case None => Results.NotFound("vet not found")
      case Some(data) => Results.Ok(Json.toJson(data))
    }
  }

}
