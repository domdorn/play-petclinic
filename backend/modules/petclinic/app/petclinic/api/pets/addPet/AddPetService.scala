package petclinic.api.pets.addPet

import java.time.LocalDate

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.{OwnerId, PetType}
import petclinic.model.owners.owner.Protocol.{AddPetCommand, AddPetResponse, OwnerNotFoundPetAddedResponse, PetAddedResponse}

import scala.concurrent.{ExecutionContext, Future}

class AddPetService @Inject()(@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) owner: ActorRef)(implicit ec: ExecutionContext) {

  def addPet(ownerId: OwnerId, nameIn: String, birthdayIn: LocalDate, `type`: PetType): Future[Option[AddPetApiResponse]] = {
    akka.pattern.Patterns.ask(owner, AddPetCommand(ownerId.id, nameIn, birthdayIn, `type`.getId, `type`.getName), Constants.TIMEOUT)
      .mapTo[AddPetResponse]
      .map {
        case PetAddedResponse(id, name, birthday, petTypeId) => Some(AddPetApiResponse(id))
        case OwnerNotFoundPetAddedResponse => None
      }
  }

}
