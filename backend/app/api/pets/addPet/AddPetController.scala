package api.pets.addPet

import javax.inject.Inject

import api.model._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._

class AddPetController @Inject()(addPetService: AddPetService, cc: ControllerComponents) extends AbstractController(cc) with PetTypeJsonSupport {
  implicit val ec = cc.executionContext
  def addPet(ownerId: String) = cc.actionBuilder.async(cc.parsers.json) { req =>

    val pet = req.body.as[Pet]

    addPetService.addPet(OwnerId.apply(ownerId), pet.name, pet.birthDate.toLocalDate, pet.`type`)
      .toScala.map(_.asScala).map {
      case None => NotFound("owner not found")
      case Some(resp) => Created(resp.id)
    }

    //    Future.successful(Created(Json.toJson(pet)))
  }


}
