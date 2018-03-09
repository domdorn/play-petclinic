package petclinic.api.specialty.getAllSpecialties

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.Specialty
import petclinic.model.specialty.SpecialtyProtocol
import petclinic.model.specialty.SpecialtyProtocol.GetAllResponse

import scala.concurrent.{ExecutionContext, Future}

class GetAllSpecialtiesService @Inject()(@Named(petclinic.api.Constants.SPECIALTIES_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {
  def getAll(): Future[Seq[Specialty]] = {
    akka.pattern.Patterns.ask(actorRef, SpecialtyProtocol.GetAllQuery, Constants.TIMEOUT)
      .mapTo[GetAllResponse]
      .map(x => x.types.map(z => new Specialty(z.id, z.name)))
  }

}
