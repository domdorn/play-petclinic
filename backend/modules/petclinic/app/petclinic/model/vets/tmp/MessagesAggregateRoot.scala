package petclinic.model.vets.tmp

import akka.persistence.PersistentActor

class MessagesAggregateRoot extends PersistentActor {
  override def persistenceId: String = "messages-aggregate"
  var messages: Seq[String] = Seq()
  override def receiveCommand: Receive = {
    case x @ "some message" => persist(x) { msg =>
      receiveRecover.apply(msg)
    }
  }
  override def receiveRecover: Receive = {
    case x: String => messages = messages ++ Seq(x)
  }
}


