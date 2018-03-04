package petclinic.api.owners.getAllOwners

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._

class GetAllOwnersController @Inject()(service: GetAllOwnersService, cc: ControllerComponents) extends AbstractController(cc) with JsonSupport {
  implicit val ec = cc.executionContext


  def getOwners = cc.actionBuilder.async(cc.parsers.json) { request =>
    service
      .getOwners()
      .toScala
      .map(_.asScala)
      .map(o => Results.Ok(Json.toJson(o)))
      .recover {
        case x => Results.BadRequest
      }
  }

}