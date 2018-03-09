package petclinic.api.pettype.getAllPetTypes

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.PetType
import petclinic.model.pettypes.PetTypeProtocol
import petclinic.model.pettypes.PetTypeProtocol.GetAllResponse

import scala.concurrent.{ExecutionContext, Future}

class GetAllPetTypeService @Inject()(@Named(petclinic.api.Constants.PET_TYPES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {
  def getAll(): Future[Seq[PetType]] = {
    akka.pattern.Patterns.ask(actorRef, PetTypeProtocol.GetAllQuery, Constants.TIMEOUT)
      .mapTo[GetAllResponse]
      .map(x => x.types.map(z => new PetType(z.id, z.name)))
  }

}
