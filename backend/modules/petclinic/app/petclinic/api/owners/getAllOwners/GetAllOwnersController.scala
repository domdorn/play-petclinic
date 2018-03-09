package petclinic.api.owners.getAllOwners

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

class GetAllOwnersController @Inject()(service: GetAllOwnersService, cc: ControllerComponents) extends AbstractController(cc) with JsonSupport {
  implicit val ec = cc.executionContext


  def getOwners = cc.actionBuilder.async { request =>
    service
      .getOwners()
      .map(o => Results.Ok(Json.toJson(o)))
      .recover {
        case x => Results.BadRequest
      }
  }

}