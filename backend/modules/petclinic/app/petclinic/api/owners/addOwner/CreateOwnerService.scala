package petclinic.api.owners.addOwner

import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import petclinic.api.Constants
import petclinic.api.model.OwnerId
import petclinic.model.owners.owner.Protocol.CreateOwnerCommand

import scala.concurrent.{ExecutionContext, Future}

class CreateOwnerService @Inject() (@Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) actorRef: ActorRef)(implicit val ec: ExecutionContext) {

  def create(firstName: String, lastName: String, address: String, city: String, telephone: String): Future[OwnerId] = {
    akka.pattern.Patterns.ask(actorRef, CreateOwnerCommand(firstName, lastName, address, city, telephone), Constants.TIMEOUT)
      .mapTo[String].map(s => OwnerId(s))
  }
}
