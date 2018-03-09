package petclinic.api.specialty.changeSpecialtyName

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.Specialty
import petclinic.model.specialty.SpecialtyProtocol._

import scala.concurrent.{ExecutionContext, Future}

class ChangeSpecialtyNameService @Inject()(@Named(petclinic.api.Constants.SPECIALTIES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def changeName(idIn: String, nameIn: String): Future[Option[Specialty]] = {
    akka.pattern.Patterns.ask(actorRef, ChangeSpecialtyNameCommand(idIn, nameIn), Constants.TIMEOUT)
      .mapTo[SpecialtyNameChangeResponse]
      .flatMap {
        case SpecialtyToChangeNotFoundResponse => Future.successful(None)
        case SpecialtyNameConflictResponse => Future.failed(new IllegalArgumentException("Specialty name already in use"))
        case SpecialtyNameChangedResponse(id, name) => Future.successful(Some(Specialty(id, name)))
      }
  }

}
