package petclinic.api.pets.getPetDetail

import javax.inject.Inject

import petclinic.api.model._
import play.api.libs.json._
import play.api.mvc._

class GetPetDetailController @Inject()(service: GetPetDetailService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext
  implicit val GetPetDetailResponseOwnerWrites: OWrites[GetPetDetailApiResponseOwner] = Json.writes[GetPetDetailApiResponseOwner]
  implicit val GetPetDetailResponseWrites: OWrites[GetPetDetailApiResponse] = Json.writes[GetPetDetailApiResponse]

  def get(ownerId: String, petId: String): Action[AnyContent] = cc.actionBuilder.async { req =>
    service.getPetDetail(OwnerId(ownerId), petId).map {
      case None => Results.NotFound("owner or pet not found")
      case Some(data) => Results.Ok(Json.toJson(data))
    }
  }

}
