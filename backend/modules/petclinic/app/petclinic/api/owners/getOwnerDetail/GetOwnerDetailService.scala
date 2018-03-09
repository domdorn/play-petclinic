package petclinic.api.owners.getOwnerDetail

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.owners.owner.Protocol.{GetOwnerDetailsQuery, GetOwnerDetailsResponse}

import scala.concurrent.{ExecutionContext, Future}

class GetOwnerDetailService @Inject() (@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) actorRef: ActorRef)(implicit ec: ExecutionContext){

  def getOwner(ownerId: String): Future[OwnerGetDetailApiResponse] = {
    akka.pattern.Patterns.ask(actorRef, GetOwnerDetailsQuery(ownerId), Constants.TIMEOUT)
      .mapTo[GetOwnerDetailsResponse]
      .map(o => OwnerGetDetailApiResponse(o.id, o.firstName, o.lastName, o.address, o.city, o.telephone, o.pets.map(p => {
        OwnerGetDetailedPet(p.id, p.name, p.birthDay, p.`type`, OwnerGetDetailedPetOwner(o.id))
      })))
  }
}
