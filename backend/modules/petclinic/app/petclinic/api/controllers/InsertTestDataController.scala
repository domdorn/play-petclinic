package petclinic.api.controllers

import java.time.{LocalDate, Month}
import javax.inject.Inject

import petclinic.api.model.OwnerId
import petclinic.api.owners.createOwner.CreateOwnerService
import petclinic.api.owners.getAllOwners.{GetAllOwnersService, OwnerGetApiResponse}
import petclinic.api.pets.addPet.AddPetService
import petclinic.api.pettype.addPetType.AddPetTypeService
import petclinic.api.pettype.getAllPetTypes.GetAllPetTypeService
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.{ExecutionContext, Future}

class InsertTestDataController @Inject()(
                                          createOwnerService: CreateOwnerService,
                                          getAllOwnersService: GetAllOwnersService,
                                          addPetService: AddPetService,
                                          addPetTypeService: AddPetTypeService,
                                          getAllPetTypeService: GetAllPetTypeService,
                                          controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {

  private val Cat = "Katze"
  private val Dog = "Hund"
  val Lizard = "Eidechse"
  val Snake = "Schlange"
  val Crocodile = "Krokodil"
  val Elephant = "Elefant"
  val Hamster = "Hamster"
  val Mouse = "Maus"

  def insertPetTypes() = controllerComponents.actionBuilder.async { req =>
    Future.sequence(
      Seq(
        addPetTypeService.create(Cat),
        addPetTypeService.create(Dog),
        addPetTypeService.create(Lizard),
        addPetTypeService.create(Snake),
        addPetTypeService.create(Crocodile),
        addPetTypeService.create(Elephant),
        addPetTypeService.create(Hamster),
        addPetTypeService.create(Mouse)
      ).map(_.toScala)
    ).map(petTypes => Results.Ok("done"))
  }

  def insertOwners() = controllerComponents.actionBuilder.async { req =>
    Future.sequence(
      Seq(
        createOwnerService.create("Dominik", "Dorn", "EineStrasse 123", "Wien", "0699123456789"),
        createOwnerService.create("Dominiks Mama", "Dorn", "Liechtensteinerstrasse 109", "Feldkirch", "0664123456789"),
        createOwnerService.create("Daniel", "Jahre", "Andere Strasse 5", "Wien", "3736262"),
        createOwnerService.create("Sigi", "Göschl", "Irgendwo in Wieden 3", "Wien", "01118"),
        createOwnerService.create("Marc", "Edem", "In da City 1", "Wien Innere Stadt", "12345"),
        createOwnerService.create("Helmuth", "Breitenfellner", "Seestadt 1", "Wien", "58362"),
        createOwnerService.create("Bela", "Juhasz", "Graben 15", "Wien", "58362")
      ).map(_.toScala)
    ).map(ids => Results.Ok(Json.toJson(ids)))
  }

  def insertPets() = controllerComponents.actionBuilder.async { req =>
    val x = getAllOwnersService.getOwners.toScala
      .zip(getAllPetTypeService.getAll.toScala)
      .map { case (owners, typesJ) => {
        val types = typesJ.asScala.map(x => x.getName -> x).toMap
        owners.asScala.map {
          case entry@OwnerGetApiResponse(_, "Dominik", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Phoebe", LocalDate.of(2013, Month.SEPTEMBER, 9), types(Cat)).toScala,
            addPetService.addPet(OwnerId(entry.id), "Imuk", LocalDate.of(2005, Month.OCTOBER, 15), types(Dog)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Dominiks Mama", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Pepino", LocalDate.of(2015, Month.APRIL, 9), types(Cat)).toScala,
            addPetService.addPet(OwnerId(entry.id), "Croco", LocalDate.of(2005, Month.APRIL, 9), types(Cat)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Daniel", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Rex", LocalDate.of(2001, Month.MAY, 5), types(Dog)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Sigi", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Klaus", LocalDate.of(2016, Month.NOVEMBER, 18), types(Hamster)).toScala,
            addPetService.addPet(OwnerId(entry.id), "Katze", LocalDate.of(2014, Month.MARCH, 5), types(Cat)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Marc", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Dexter", LocalDate.of(2017, Month.DECEMBER, 24), types(Mouse)).toScala,
            addPetService.addPet(OwnerId(entry.id), "Jon", LocalDate.of(2012, Month.FEBRUARY, 2), types(Dog)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Helmuth", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Herkules", LocalDate.of(2008, Month.JUNE, 12), types(Lizard)).toScala
          ))
          case entry@OwnerGetApiResponse(_, "Bela", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Fred", LocalDate.of(2015, Month.JUNE, 9), types(Cat)).toScala
          ))
          case x: OwnerGetApiResponse => Future.successful(Nil)
        }
      }
      }.flatMap(y => Future.sequence(y.toSeq)).map(seqOfSeq => seqOfSeq.flatten.map(_.asScala))

    x.map(responses => Results.Ok(Json.toJson(responses)))
  }


}
