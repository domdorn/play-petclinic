package petclinic.model.owners

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, ReceiveTimeout}
import petclinic.model.owners.owner.Protocol.{OverviewStateQuery, OverviewStateResponseComplete, OverviewStateResponseIncomplete, OwnerOverviewStateResponse}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object OwnerGetOverviewActor {
  def props(owners: Seq[ActorRef], recipient: ActorRef): Props = Props(new OwnerGetOverviewActor(owners, recipient))
}

class OwnerGetOverviewActor(owners: Seq[ActorRef], recipient: ActorRef) extends Actor with ActorLogging {
  implicit val ec: ExecutionContext = context.dispatcher

  println("starting ownergetoverviewactor")

  owners.foreach(ref => ref ! OverviewStateQuery)

  var waitingForReply: Vector[ActorRef] = owners.toVector
  var data: Vector[OwnerOverviewStateResponse] = Vector.empty

  val requestTimeout: Option[Cancellable] = if (waitingForReply.nonEmpty) {
    Some(context.system.scheduler.scheduleOnce(500 millis, self, "shouldBeDone"))
  } else {
    self ! "shouldBeDone"
    None
  }

  context.setReceiveTimeout(3 seconds)

  var answerTold = false

  override def receive: Receive = {
    case "shouldBeDone" => {
      if (waitingForReply.isEmpty) {
        println("collected all the answers")
        recipient ! OverviewStateResponseComplete(data)
        answerTold = true
      } else {
        println("we're still waiting for responses from these children:")
        println(waitingForReply)
        println("sending incomplete data")
        recipient ! OverviewStateResponseIncomplete(data, waitingForReply)
        answerTold = true
      }
    }
    case x: OwnerOverviewStateResponse => {
      if (waitingForReply.contains(sender())) {
        waitingForReply = waitingForReply.filterNot(_ == sender())
        data = data :+ x
        println(s"answer received from ${sender()}")
      }
      if (waitingForReply.isEmpty) {
        println("aaaand we're done!")
        recipient ! OverviewStateResponseComplete(data)
        requestTimeout.foreach(x => x.cancel())
        answerTold = true
      }
    }
    case x : ReceiveTimeout => {
      if(!answerTold) {
        log.warning("OwnerGetOverviewActor still alive after 3 seconds, but no answer has been sent!")
      }
      context.stop(self)
    }
  }
}
