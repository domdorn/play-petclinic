package petclinic.api.specialty.getSpecialty

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.Specialty
import petclinic.model.specialty.SpecialtyProtocol._

import scala.concurrent.{ExecutionContext, Future}

class GetSpecialtyService @Inject()(@Named(petclinic.api.Constants.SPECIALTIES_ACTOR_NAME) actor: ActorRef)(implicit ec: ExecutionContext) {

  def getSpecialty(idIn: String): Future[Option[Specialty]] = {
    akka.pattern.Patterns.ask(actor, GetQuery(idIn), Constants.TIMEOUT)
      .mapTo[GetQueryResponse]
      .map {
        case FoundGetQueryResponse(id, name) => Some(new Specialty(id, name))
        case NotFoundGetQueryResponse(id) => None
      }
  }

}
