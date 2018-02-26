package api.pets.getPetDetail

import javax.inject.Inject

import api.model._
import play.api.libs.json._
import play.api.mvc._

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

class GetPetDetailController @Inject()(service: GetPetDetailService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext
  implicit val GetPetDetailResponseOwnerWrites: OWrites[GetPetDetailApiResponseOwner] = Json.writes[GetPetDetailApiResponseOwner]
  implicit val GetPetDetailResponseWrites: OWrites[GetPetDetailApiResponse] = Json.writes[GetPetDetailApiResponse]

  def getPet(ownerId: String, petId: String): Action[AnyContent] = cc.actionBuilder.async { req =>
    service.getPetDetail(OwnerId(ownerId), petId).toScala.map(_.asScala).map {
      case None => Results.NotFound("owner or pet not found")
      case Some(data) => Results.Ok(Json.toJson(data))
    }
  }

}
