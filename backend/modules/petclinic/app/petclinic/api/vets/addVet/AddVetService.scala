package petclinic.api.vets.addVet

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.vets.Protocol.{CreateVetResponse, VetCouldNotBeCreatedResponse, VetCreatedResponse}

import scala.concurrent.{ExecutionContext, Future}

final class AddVetService @Inject()(@Named(petclinic.api.Constants.VETS_AGGREGATE_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {

  private[addVet] def add(body: AddVetApiRequest): Future[AddVetApiResponse] = {
    add(body.firstName, body.lastName)
  }

  def add(firstNameIn: String, lastNameIn: String): Future[AddVetApiResponse] = {
    akka.pattern.ask(
      actorRef,
      petclinic.model.vets.Protocol.CreateVetCommand(firstNameIn, lastNameIn, Nil)
    )(Constants.TIMEOUT)
      .mapTo[CreateVetResponse]
      .map {
        case VetCreatedResponse(id, firstName, lastName, specialties) => AddVetApiResponse(id, firstName, lastName)
        case VetCouldNotBeCreatedResponse => throw new IllegalArgumentException("Vet could not be created")
      }

  }

}
