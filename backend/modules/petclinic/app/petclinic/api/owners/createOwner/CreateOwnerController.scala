package petclinic.api.owners.createOwner

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.compat.java8.FutureConverters._

class CreateOwnerController @Inject()(service: CreateOwnerService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val ec = cc.executionContext


  def createOwner = cc.actionBuilder.async(cc.parsers.json) { request =>

    val owner = request.body.as[OwnerCreate]

    service
      .create(owner.firstName, owner.lastName, owner.address, owner.city, owner.telephone)
      .toScala
      .map(o => Results.Created(Json.toJson(o)))
      .recover {
        case x => Results.BadRequest
      }
  }

}
