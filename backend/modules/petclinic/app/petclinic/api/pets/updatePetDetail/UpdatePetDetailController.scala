package petclinic.api.pets.updatePetDetail

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import petclinic.api.model._
import play.api.libs.json._
import play.api.mvc._

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.util.{Failure, Success, Try}


case class UpdatePetDetailsApiJsonRequestOwner(id: String)
case class UpdatePetDetailsApiJsonRequest(id: String, name: String, birthDate: LocalDate, owner: UpdatePetDetailsApiJsonRequestOwner, `type`: PetType)

object UpdatePetDetailsApiJsonRequest extends PetTypeJsonSupport {
  import play.api.libs.json._ // JSON library
  import play.api.libs.json.Reads._ // Custom validation helpers
  import play.api.libs.functional.syntax._ // Combinator syntax

  val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
  implicit val readLocalDate = Reads[LocalDate]{ json => {
    Try(LocalDate.parse(json.as[String], formatter)) match {
      case Success(value) => JsSuccess(value)
      case Failure(ex) => JsError("could not parse date: " + ex.getMessage)
    }
  }}
  implicit val UpdatePetDetailsApiJsonRequestOwnerReads: Reads[UpdatePetDetailsApiJsonRequestOwner] = Json.reads[UpdatePetDetailsApiJsonRequestOwner]

  implicit val updatePetDetailsApiJsonRequestReads: Reads[UpdatePetDetailsApiJsonRequest] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "birthDate").read[LocalDate](readLocalDate) and
      (JsPath \ "owner").read[UpdatePetDetailsApiJsonRequestOwner] and
      (JsPath \ "type").read[PetType]
    )(UpdatePetDetailsApiJsonRequest.apply _)

}

class UpdatePetDetailController @Inject()(service: UpdatePetDetailService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def updatePetDetails(ownerId: String, petId: String): Action[UpdatePetDetailsApiJsonRequest] = cc.actionBuilder(cc.parsers.json[UpdatePetDetailsApiJsonRequest]).async { req =>
    val payload = req.body

    service
      .updatePetDetails(OwnerId(ownerId), petId, payload.name, payload.birthDate, payload.`type`.getId)
      .toScala
      .map(_.asScala)
      .map {
      case None => BadRequest("owner or pet not found")
      case Some(update) => NoContent
    }
  }

}
