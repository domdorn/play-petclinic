package petclinic.api.owners.getOwnerDetail

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.compat.java8.FutureConverters._

class GetOwnerDetailController @Inject()(service: GetOwnerDetailService, cc: ControllerComponents) extends AbstractController(cc) with JsonSupport {
  implicit val ec = cc.executionContext

  def getOwner(ownerId: String) = cc.actionBuilder.async { request =>
    service.getOwner(ownerId).toScala.map(o => Results.Ok(Json.toJson(o)))
  }

}