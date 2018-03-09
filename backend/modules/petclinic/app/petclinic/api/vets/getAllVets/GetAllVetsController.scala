package petclinic.api.vets.getAllVets

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.concurrent.ExecutionContext

class GetAllVetsController @Inject() (
                                       service: GetAllVetsService,
                                       cc: ControllerComponents)
                                     (implicit val ec: ExecutionContext) extends AbstractController(cc) {


  def getAll() = cc.actionBuilder.async {
    import GetAllVetsApiResult.petFormat
    service.getAll().map{ x => Results.Ok(Json.toJson(x.data))}
  }

}
