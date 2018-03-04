package petclinic.api.pettype.deletePetType

import javax.inject.Inject

import petclinic.api.model.PetTypeJsonSupport
import play.api.mvc._

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

class DeletePetTypeController @Inject()(petTypeService: DeletePetTypeService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext

  def deletePetType(id: String) = cc.actionBuilder.async { req =>
    petTypeService.deleteType(id).toScala.map(opt => opt.asScala).map {
      case Some(worked) => if (worked) Results.NoContent else Results.Forbidden("type still in use")
      case None => Results.BadRequest("error")
    }
  }


}
