package petclinic.api.vets.getAllVets

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.vets.Protocol
import petclinic.model.vets.Protocol.GetAllVetsReponse

import scala.concurrent.{ExecutionContext, Future}

class GetAllVetsService @Inject()(@Named(petclinic.api.Constants.VETS_AGGREGATE_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext) {
  def getAll(): Future[GetAllVetsApiResult] = {
    akka.pattern.ask(actorRef, Protocol.GetAllVets)(Constants.TIMEOUT).mapTo[GetAllVetsReponse]
      .map(x => GetAllVetsApiResult(x.vets.map(v => Vet(v.id, v.firstName, v.lastName))))
  }


}
