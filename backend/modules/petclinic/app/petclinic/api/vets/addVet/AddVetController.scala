package petclinic.api.vets.addVet

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.concurrent.ExecutionContext

private[addVet] case class AddVetApiRequest(firstName: String, lastName: String)
private[addVet] case class AddVetApiResponse(id: String, firstName: String, lastName: String)

final class AddVetController @Inject()(addVetService: AddVetService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val reqFormat = Json.reads[AddVetApiRequest]
  implicit val resFormat = Json.writes[AddVetApiResponse]

  implicit val ec: ExecutionContext = cc.executionContext
  def add = cc.actionBuilder.async(cc.parsers.json[AddVetApiRequest]) { req =>
    addVetService.add(req.body).map( x => Results.Created(Json.toJson(x)))
  }

}
