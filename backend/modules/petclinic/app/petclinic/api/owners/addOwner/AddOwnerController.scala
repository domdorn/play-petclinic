package petclinic.api.owners.addOwner

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AddOwnerController @Inject()(service: CreateOwnerService,
                                   cc: ControllerComponents, actorSystem: ActorSystem,
                                   @Named(petclinic.api.Constants.OWNER_AGGREGATE_ACTOR_NAME) owner: ActorRef) extends AbstractController(cc) {
  implicit val ec: ExecutionContext = cc.executionContext

  def add(): Action[OwnerCreate] = cc.actionBuilder.async(cc.parsers.json[OwnerCreate]) { request =>
    val owner = request.body

    service
      .create(owner.firstName, owner.lastName, owner.address, owner.city, owner.telephone)
      .map(o => Results.Created(Json.toJson(o)))
      .recover {
        case x => Results.BadRequest
      }
  }

}
