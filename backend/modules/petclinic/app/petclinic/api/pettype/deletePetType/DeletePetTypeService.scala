package petclinic.api.pettype.deletePetType

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.pettypes.PetTypeProtocol._

import scala.concurrent.{ExecutionContext, Future}

class DeletePetTypeService @Inject()(@Named(petclinic.api.Constants.PET_TYPES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def deleteType(id: String): Future[Option[Boolean]] = {
    akka.pattern.Patterns.ask(actorRef, DeletePetTypeCommand(id), Constants.TIMEOUT)
      .mapTo[PetTypeDeleteResponse]
      .map {
        case PetTypeToDeleteNotFoundResponse => Some(true)
        case PetTypeDeletedResponse => None
        case PetTypeStillInUseResponse => Some(false)
      }
  }
}
