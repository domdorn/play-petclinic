package petclinic.api.pettype.changePetTypeName

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.PetType
import petclinic.model.pettypes.PetTypeProtocol._

import scala.concurrent.{ExecutionContext, Future}

class ChangePetTypeNameService @Inject()(@Named(petclinic.api.Constants.PET_TYPES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def changeName(idIn: String, nameIn: String): Future[Option[PetType]] = {
    akka.pattern.Patterns.ask(actorRef, ChangePetTypeNameCommand(idIn, nameIn), Constants.TIMEOUT)
      .mapTo[PetTypeNameChangeResponse]
      .flatMap {
        case PetTypeToChangeNotFoundResponse => Future.successful(None)
        case PetTypeNameConflictResponse => Future.failed(new IllegalArgumentException("pettype name already in use"))
        case PetTypeNameChangedResponse(id, name) => Future.successful(Some(new PetType(id, name)))
      }
  }

}
