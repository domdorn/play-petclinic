package petclinic.model.owners

import akka.actor.ActorRef
import akka.pattern.{AskSupport, PipeToSupport}
import akka.persistence.PersistentActor
import akka.util.Timeout
import petclinic.model.owners
import petclinic.model.owners.OwnerRepository.OwnerIdCreatedEvent
import tyrex.services.UUID

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object OwnerRepository {
  private[model] case class OwnerIdCreatedEvent(uuid: String)
}

class OwnerRepository extends PersistentActor with AskSupport with PipeToSupport {
  import owners.Owner
  import owners.owner.Protocol._

  import scala.language.postfixOps
  implicit val timeout: Timeout = Timeout(1 second)
  implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId: String = "owners-aggregate"

  var childrenIds: Vector[String] = Vector.empty[String]

  override def receiveRecover: Receive = {
    case OwnerIdCreatedEvent(uuid) => {
      println("recovering children, adding id: " + uuid)
      childrenIds = childrenIds :+ uuid
      println("childrenIds is now " + childrenIds)
    }
    case x => println(s"OwnerRepository: trying to recover message $x")
  }

  def getOrCreateChild(id:String): ActorRef = context.child(id).getOrElse(context.actorOf(Owner.props(id), id))

  override def receiveCommand: Receive = {

    case x : ForwardToOwnerMessage => getOrCreateChild(x.ownerId) forward x

    case x: CreateOwnerCommand => {
      val _sender = sender()
      val uuid = UUID.create()
      getOrCreateChild(uuid) forward x
      println("received create owner command")
      persist(OwnerIdCreatedEvent(uuid)) { ev =>
        receiveRecover(ev)
        _sender ! ev.uuid
      }
    }

    case x: GetAllOwnersQuery => {
      val _sender = sender()
//      context.actorOf(OwnerGetOverviewActor.props(childrenIds.map(getOrCreateChild(_)), sender()))

      val questionsToMyChildren: Iterable[Future[OwnerOverviewStateResponse]] = childrenIds
        .map(id => getOrCreateChild(id))
        .map(child => (child ? OverviewStateQuery).mapTo[OwnerOverviewStateResponse])
      val futureResponses = Future.sequence(questionsToMyChildren) map (resp => OverviewStateResponseComplete(resp.toSeq))
      pipe(futureResponses).to(_sender)
    }

  }
}




