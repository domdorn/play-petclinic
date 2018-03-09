package petclinic.api.owners.getAllOwners

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.model.owners.owner.Protocol.{GetAllOwnersQuery, OverviewStateResponse}

import scala.concurrent.{ExecutionContext, Future}

class GetAllOwnersService @Inject()(@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) owner: ActorRef)(implicit ec: ExecutionContext) {

  def getOwners(): Future[Seq[OwnerGetApiResponse]] = akka.pattern.Patterns.ask(owner, GetAllOwnersQuery(), Constants.TIMEOUT)
    .mapTo[OverviewStateResponse].map(mapOverviewStateResponse)

  def mapOverviewStateResponse(resp: OverviewStateResponse): Seq[OwnerGetApiResponse] = {
    resp.data.map(rs =>
      OwnerGetApiResponse(
        rs.id,
        rs.firstName,
        rs.lastName,
        rs.address,
        rs.city,
        rs.telephone,
        rs.pets.map(x => OwnerGetPet(x))
      ))
  }
}
