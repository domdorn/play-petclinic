package petclinic.api.pets.updatePetDetail

import java.time.LocalDate

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.OwnerId
import petclinic.api.pettype.getPetType.GetPetTypeService
import petclinic.model.owners.owner.Protocol._

import scala.concurrent.{ExecutionContext, Future}

class UpdatePetDetailService @Inject() (@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) actorRef: ActorRef, getPetTypeService: GetPetTypeService)(implicit ec: ExecutionContext) {

  def updatePetDetails(ownerId: OwnerId, petId: String, name: String, birthDay: LocalDate, petTypeId: String): Future[Option[UpdatePetDetailApiResponse]] = {
    getPetTypeService.getPetType(petTypeId)
      .flatMap(maybePetType => {
      maybePetType.map(pt => {
        akka.pattern.Patterns.ask(actorRef, UpdatePetDetailsCommand(ownerId.id, petId, name, birthDay, pt), Constants.TIMEOUT)
          .mapTo[UpdatePetDetailsResponse]
          .map {
            case OwnerNotFoundUpdatePetDetailsResponse => None
            case PetNotFoundUpdatePetDetailsResponse => None
            case PetDetailsUpdatedResponse(petIdReceived) => Some(UpdatePetDetailApiResponse(petIdReceived))
          }
      }).getOrElse(Future.successful(None))

    })
  }

}
