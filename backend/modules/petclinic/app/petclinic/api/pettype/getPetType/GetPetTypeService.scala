package petclinic.api.pettype.getPetType

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.PetType
import petclinic.model.pettypes.PetTypeProtocol
import petclinic.model.pettypes.PetTypeProtocol.{FoundGetQueryResponse, GetQueryResponse, NotFoundGetQueryResponse}

import scala.concurrent.{ExecutionContext, Future}

class GetPetTypeService @Inject()(@Named(petclinic.api.Constants.PET_TYPES_ACTOR_NAME) actor: ActorRef)(implicit ec: ExecutionContext) {

  def getPetType(idIn: String): Future[Option[PetType]] = {
    akka.pattern.Patterns.ask(actor, PetTypeProtocol.GetQuery(idIn), Constants.TIMEOUT)
      .mapTo[GetQueryResponse]
      .map {
        case FoundGetQueryResponse(id, name) => Some(new PetType(id, name))
        case NotFoundGetQueryResponse(id) => None
      }
  }

}
