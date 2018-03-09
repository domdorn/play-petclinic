package petclinic.api.specialty.addSpecialty

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.Specialty
import petclinic.model.specialty.SpecialtyProtocol.{CreateSpecialtyCommand, SpecialtyCreateResponse}

import scala.concurrent.{ExecutionContext, Future}


class AddSpecialtyService @Inject()(@Named(petclinic.api.Constants.SPECIALTIES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  def create(name: String): Future[Specialty] = {
    akka.pattern.Patterns.ask(actorRef, CreateSpecialtyCommand(name), Constants.TIMEOUT)
      .mapTo[SpecialtyCreateResponse]
      .map(r => Specialty(r.id, r.name))
  }

}
