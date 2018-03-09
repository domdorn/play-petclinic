package petclinic.api.specialty.addSpecialty

import javax.inject.Inject
import petclinic.api.model.{PetType, Specialty}
import play.api.libs.json.Json
import play.api.mvc._

case class AddSpecialty(name: String)

class AddSpecialtyController @Inject()(petTypeService: AddSpecialtyService, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val ec = cc.executionContext

  implicit val addSpecialtyReads = Json.reads[AddSpecialty]

  def add = cc.actionBuilder.async(cc.parsers.json[AddSpecialty]) { req =>
    val specialty = req.body
    petTypeService.create(specialty.name).map(res => Results.Ok(Json.toJson(res)))
  }

}
