package petclinic.api.vets.getVetDetail

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.specialty.getAllSpecialties.GetAllSpecialtiesService
import petclinic.model.vets.Protocol.{GetVetDetailsQuery, GetVetDetailsResponse, VetDetails, GetDetailsVetNotFound}

import scala.concurrent.{ExecutionContext, Future}

class GetVetDetailService @Inject()(@Named(petclinic.api.Constants.VETS_AGGREGATE_ACTOR_NAME) owner: ActorRef, getAllSpecialtiesService: GetAllSpecialtiesService)(implicit ec: ExecutionContext) {

  def getVetDetail(vetId: String): Future[Option[GetVetDetailApiResponse]] = {
    val specialties = getAllSpecialtiesService.getAll()
    val vetDetailsFuture = akka.pattern.Patterns.ask(owner, GetVetDetailsQuery(vetId), Constants.TIMEOUT)
      .mapTo[GetVetDetailsResponse]

    specialties.zip(vetDetailsFuture).map { case (petTypes, details) =>
      details match {
        case VetDetails(id, firstName, lastName, spec) => Some(GetVetDetailApiResponse(id, firstName, lastName, spec.map(id => GetVetDetailSpecialty(id))))
        case GetDetailsVetNotFound => None
      }

    }

  }

}
