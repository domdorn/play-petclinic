package petclinic.api.vets.changeVet

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.vets.Protocol._

import scala.concurrent.{ExecutionContext, Future}

class ChangeVetService @Inject()(@Named(petclinic.api.Constants.VETS_AGGREGATE_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {
  def change(id: String, data: ChangeVetApiRequest): Future[Option[Boolean]] = {
    akka.pattern.Patterns.ask(actorRef, UpdateVetCommand(id, data.firstName, data.lastName, data.specialties.map(_.id.toString)), Constants.TIMEOUT)
      .mapTo[UpdateVetResponse]
      .map {
        case VetUpdatedResponse => Some(true)
        case VetUpdateFailedResponse => Some(false)
        case VetToUpdateNotFound => None
      }
  }

}
