package petclinic.testdata

import java.time.{LocalDate, Month}

import javax.inject.Inject
import petclinic.api.model.OwnerId
import petclinic.api.owners.addOwner.CreateOwnerService
import petclinic.api.owners.getAllOwners.{GetAllOwnersService, OwnerGetApiResponse}
import petclinic.api.pets.addPet.AddPetService
import petclinic.api.pettype.addPetType.AddPetTypeService
import petclinic.api.pettype.getAllPetTypes.GetAllPetTypeService
import petclinic.api.specialty.addSpecialty.AddSpecialtyService
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.concurrent.{ExecutionContext, Future}


object InsertTestDataController {

  object PetTypes {
    val Cat = "Katze"
    val Dog = "Hund"
    val Lizard = "Eidechse"
    val Snake = "Schlange"
    val Crocodile = "Krokodil"
    val Elephant = "Elefant"
    val Hamster = "Hamster"
    val Mouse = "Maus"
  }

  object Specialties {
    // taken from https://kampfschmuser.de/t/fachrichtungen-in-der-veterinaermedizin.40004/
    val Anatomie = "Anatomie"
    val BacteriesAndMykology = "Bakteriologie und Mykologie"
    val fishes = "Fische"
    val meatHygene = "Fleischhygiene"
    val poultry = "Geflügel"
    val smallAnimals = "Kleintiere"
    val clinicalLabDiagnostics = "Klinische Laboratoriumsdiagnostik"
    val food = "Lebensmittel"
    //    1.8Lebensmittel
    val milkHygeneAndTechnology = "Hygiene und Technologie der Milch"
    val parasitology = "Parasitologie"
    val pathology = "Pathologie"
    //      1.11.1 Teilgebiet Toxikopathologie
    val horses = "Pferde"
    //      1.12.1 Teilgebiet Chirurgie
    //      1.12.2 Teilgebiet Innere Medizin
    //    1.12.3 Teilgebiet Orthopädie
    val pharmacologyAndToxicology = "Pharmakologie und Toxikologie"
    val physiologyAndPhysiologicalChemistry = "Physiologie und Physiologische Chemie"
    val reproductionMedicine = "Reproduktionsmedizin"
    val cattle = "Rinder"
    val sheepsAndGoats = "Schafe und Ziegen"
    val pigs = "Schweine"
    val animalNutritionAndDietetics = "Tierernährung und Diäthetik"
    val animalHygiene = "Tierhygiene"
    val tropicalVeterinarymedicine = "Tropenveterinärmedizin"
    val experimentalAnimalScience = "Versuchstierkunde"
    val virology = "Virologie"
    val zooAndWildAnimals = "Zoo- und Wildtiere"
    val publicVeterniary = "Öffentliches Veterinärwesen"
    val animalProtection = "Tierschutz"
  }

}

class InsertTestDataController @Inject()(
                                          createOwnerService: CreateOwnerService,
                                          getAllOwnersService: GetAllOwnersService,
                                          addPetService: AddPetService,
                                          addPetTypeService: AddPetTypeService,
                                          addSpecialtyService: AddSpecialtyService,
                                          getAllPetTypeService: GetAllPetTypeService,
                                          controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {

  import InsertTestDataController.PetTypes._


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
      )
    ).map(petTypes => Results.Ok("done"))
  }

  def insertSpecialties() = controllerComponents.actionBuilder.async { req =>
    import InsertTestDataController.Specialties._
    Future.sequence(
      Seq(
        addSpecialtyService.create(Anatomie),
        addSpecialtyService.create(BacteriesAndMykology),
        addSpecialtyService.create(fishes),
        addSpecialtyService.create(meatHygene),
        addSpecialtyService.create(poultry),
        addSpecialtyService.create(smallAnimals),
        addSpecialtyService.create(clinicalLabDiagnostics),
        addSpecialtyService.create(food),
        addSpecialtyService.create(milkHygeneAndTechnology),
        addSpecialtyService.create(parasitology),
        addSpecialtyService.create(pathology),
        addSpecialtyService.create(horses),
        addSpecialtyService.create(pharmacologyAndToxicology),
        addSpecialtyService.create(physiologyAndPhysiologicalChemistry),
        addSpecialtyService.create(reproductionMedicine),
        addSpecialtyService.create(cattle),
        addSpecialtyService.create(sheepsAndGoats),
        addSpecialtyService.create(pigs),
        addSpecialtyService.create(animalNutritionAndDietetics),
        addSpecialtyService.create(animalHygiene),
        addSpecialtyService.create(tropicalVeterinarymedicine),
        addSpecialtyService.create(experimentalAnimalScience),
        addSpecialtyService.create(virology),
        addSpecialtyService.create(zooAndWildAnimals),
        addSpecialtyService.create(publicVeterniary),
        addSpecialtyService.create(animalProtection)
      )
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
      )
    ).map(ids => Results.Ok(Json.toJson(ids)))
  }

  def insertPets() = controllerComponents.actionBuilder.async { req =>
    val x = getAllOwnersService.getOwners
      .zip(getAllPetTypeService.getAll)
      .map { case (owners, typesJ) => {
        val types = typesJ.map(x => x.getName -> x).toMap
        owners.map {
          case entry@OwnerGetApiResponse(_, "Dominik", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Phoebe", LocalDate.of(2013, Month.SEPTEMBER, 9), types(Cat)),
            addPetService.addPet(OwnerId(entry.id), "Imuk", LocalDate.of(2005, Month.OCTOBER, 15), types(Dog))
          ))
          case entry@OwnerGetApiResponse(_, "Dominiks Mama", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Pepino", LocalDate.of(2015, Month.APRIL, 9), types(Cat)),
            addPetService.addPet(OwnerId(entry.id), "Croco", LocalDate.of(2005, Month.APRIL, 9), types(Cat))
          ))
          case entry@OwnerGetApiResponse(_, "Daniel", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Rex", LocalDate.of(2001, Month.MAY, 5), types(Dog))
          ))
          case entry@OwnerGetApiResponse(_, "Sigi", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Klaus", LocalDate.of(2016, Month.NOVEMBER, 18), types(Hamster)),
            addPetService.addPet(OwnerId(entry.id), "Katze", LocalDate.of(2014, Month.MARCH, 5), types(Cat))
          ))
          case entry@OwnerGetApiResponse(_, "Marc", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Dexter", LocalDate.of(2017, Month.DECEMBER, 24), types(Mouse)),
            addPetService.addPet(OwnerId(entry.id), "Jon", LocalDate.of(2012, Month.FEBRUARY, 2), types(Dog))
          ))
          case entry@OwnerGetApiResponse(_, "Helmuth", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Herkules", LocalDate.of(2008, Month.JUNE, 12), types(Lizard))
          ))
          case entry@OwnerGetApiResponse(_, "Bela", _, _, _, _, _) => Future.sequence(Seq(
            addPetService.addPet(OwnerId(entry.id), "Fred", LocalDate.of(2015, Month.JUNE, 9), types(Cat))
          ))
          case x: OwnerGetApiResponse => Future.successful(Nil)
        }
      }
      }.flatMap(y => Future.sequence(y.toSeq)).map(seqOfSeq => seqOfSeq.flatten)

    x.map(responses => Results.Ok(Json.toJson(responses)))
  }


}
