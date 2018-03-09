package petclinic.api.pets.getPetDetail

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.OwnerId
import petclinic.api.pettype.getAllPetTypes.GetAllPetTypeService
import petclinic.model.owners.owner.Protocol._

import scala.concurrent.{ExecutionContext, Future}

class GetPetDetailService @Inject()(@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) owner: ActorRef, getAllPetTypeService: GetAllPetTypeService)(implicit ec: ExecutionContext) {

  def getPetDetail(ownerIdIn: OwnerId, petIdIn: String): Future[Option[GetPetDetailApiResponse]] = {
    val typesFuture = getAllPetTypeService.getAll()
    val petDetailsFuture = akka.pattern.Patterns.ask(owner, GetPetDetailsQuery(ownerIdIn.id, petIdIn), Constants.TIMEOUT)
      .mapTo[GetPetDetailsResponse]

    typesFuture.zip(petDetailsFuture).map { case (petTypes, details) =>
      details match {
        case OwnerNotFoundGetPetDetailsResponse => None
        case PetNotFoundGetPetDetailsresponse => None
        case PetDetailsResponse(ownerId, ownerFirstName, ownerLastName, petId, petName, petBirthDay, petTypeId) =>
          Some(GetPetDetailApiResponse(
            petId,
            petName,
            petBirthDay,
            petTypes.find(x => x.getId.equals(petId)).get,
            GetPetDetailApiResponseOwner(ownerId, ownerFirstName, ownerLastName)
          ))
      }
    }

  }

}
