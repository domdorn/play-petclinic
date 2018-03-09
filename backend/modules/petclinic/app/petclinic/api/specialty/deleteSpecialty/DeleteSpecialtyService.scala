package petclinic.api.specialty.deleteSpecialty

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.specialty.SpecialtyProtocol._

import scala.concurrent.{ExecutionContext, Future}

class DeleteSpecialtyService @Inject()(@Named(petclinic.api.Constants.SPECIALTIES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def delete(id: String): Future[Option[Boolean]] = {
    akka.pattern.Patterns.ask(actorRef, DeleteSpecialtyCommand(id), Constants.TIMEOUT)
      .mapTo[SpecialtyDeleteResponse]
      .map {
        case SpecialtyToDeleteNotFoundResponse => None
        case SpecialtyDeletedResponse => Some(true)
        case SpecialtyStillInUseResponse => Some(false)
      }
  }
}
