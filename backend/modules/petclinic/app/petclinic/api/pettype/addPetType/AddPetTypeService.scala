package petclinic.api.pettype.addPetType

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.PetType
import petclinic.model.pettypes.PetTypeProtocol.{CreatePetTypeCommand, PetTypeCreateResponse}

import scala.concurrent.{ExecutionContext, Future}


class AddPetTypeService @Inject()(@Named(petclinic.api.Constants.PET_TYPES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def create(name: String): Future[PetType] = {
    akka.pattern.Patterns.ask(actorRef, CreatePetTypeCommand(name), Constants.TIMEOUT)
      .mapTo[PetTypeCreateResponse]
      .map(r => new PetType(r.id, r.name))
  }

}
