package petclinic.model.specialty

import akka.persistence.PersistentActor

object SpecialtyProtocol {

  sealed trait Query
  sealed trait Command
  sealed trait Response

  // use case 1: create a new pet type
  case class CreateSpecialtyCommand(name: String) extends Command
  sealed trait SpecialtyCreateResponse extends Response {
    val id: String
    val name: String
  }
  case class SpecialtyCreatedResponse(id: String, name: String) extends SpecialtyCreateResponse
  case class SpecialtyAlreadyExistsResponse(id: String, name: String) extends SpecialtyCreateResponse

  // use case 2: get all pet types
  case object GetAllQuery extends Query
  case class GetAllResponse(types: Seq[GetAllSpecialtyResponseContainer]) extends Response

  case class GetAllSpecialtyResponseContainer(id: String, name: String)


  // use case 3: get a single pet type:
  sealed trait GetQueryResponse extends Response
  case class GetQuery(id: String) extends Query
  case class FoundGetQueryResponse(id: String, name: String) extends GetQueryResponse
  case class NotFoundGetQueryResponse(id: String) extends GetQueryResponse

  // use case 4: delete a pet type
  case class DeleteSpecialtyCommand(id: String) extends Command
  sealed trait SpecialtyDeleteResponse extends Response
  case object SpecialtyToDeleteNotFoundResponse extends SpecialtyDeleteResponse
  case object SpecialtyDeletedResponse extends SpecialtyDeleteResponse
  case object SpecialtyStillInUseResponse extends SpecialtyDeleteResponse


  // use case 5: change pet type name
  case class ChangeSpecialtyNameCommand(id: String, newName: String) extends Command
  sealed trait SpecialtyNameChangeResponse extends Response
  case object SpecialtyToChangeNotFoundResponse extends SpecialtyNameChangeResponse
  case object SpecialtyNameConflictResponse extends SpecialtyNameChangeResponse
  case class SpecialtyNameChangedResponse(id: String, name: String) extends SpecialtyNameChangeResponse


  /* private[pettypes] */case class SpecialtyVO(id: String, name: String)
}

object SpecialtyEntity {
  object Events {
    sealed trait Event
    case class SpecialtyCreatedEvent(id: String, name: String) extends Event
    case class SpecialtyDeletedEvent(id: String, name: String)
    case class SpecialtyNameChangedEvent(id: String, name: String) extends Event
  }
}

class SpecialtyEntity extends PersistentActor {
  import SpecialtyEntity.Events._
  import SpecialtyProtocol._
  override def persistenceId: String = "specialties"

  var data: Map[SpecialtyVO, Int] = Map()
  var latestId = 1

  override def receiveRecover: Receive = {
    case SpecialtyCreatedEvent(id, name) =>
      data = data + (SpecialtyVO(id, name) -> 0)
      latestId = latestId + 1
    case SpecialtyDeletedEvent(id, name) => data = data - SpecialtyVO(id, name)
    case SpecialtyNameChangedEvent(id, newName) => {
      data.find(_._1.id == id) match {
        case Some(entry) => data = (data - entry._1) + (SpecialtyVO(id, newName) -> entry._2)
        case None => throw new IllegalStateException("this should not happen!")
      }
    }
  }

  override def receiveCommand: Receive = {
    case CreateSpecialtyCommand(name) => data.keySet.find(pt => pt.name == name) match {
        case Some(pet) => sender() ! SpecialtyAlreadyExistsResponse(pet.id, pet.name)
        case None =>  persist(SpecialtyCreatedEvent(latestId.toString, name)){ ev =>
          receiveRecover(ev)
          sender() ! SpecialtyCreatedResponse(ev.id, ev.name)
        }
      }

    case DeleteSpecialtyCommand(id) => data.keySet.find(_.id == id) match {
        case Some(pet) => if(data(pet) > 0) sender() ! SpecialtyStillInUseResponse else {
          val s = sender()
          persist(SpecialtyDeletedEvent(pet.id, pet.name)) { ev =>
            receiveRecover(ev)
            s ! SpecialtyDeletedResponse
          }
        }
        case None => sender() ! SpecialtyToDeleteNotFoundResponse
      }

    case ChangeSpecialtyNameCommand(id, name) => data.keySet.find(_.id == id) match {
      case None => sender() ! SpecialtyToChangeNotFoundResponse
      case Some(pet) => if(pet.name == name) {
        sender() ! SpecialtyNameChangedResponse(pet.id, pet.name)
      } else if (name.isEmpty) {
        sender() ! SpecialtyNameConflictResponse
      } else {
        data.keySet.find(_.name == name) match {
          case None => persist(SpecialtyNameChangedEvent(id, name)){ ev =>
            receiveRecover(ev)
            sender() ! SpecialtyNameChangedResponse(id, name)
            // TODO publish domain event?
          }
          case Some(existingPetWithThisName) => if(existingPetWithThisName.id == pet.id) {
            sender() ! SpecialtyNameChangedResponse(id, name)
          } else {
            sender() ! SpecialtyNameConflictResponse
          }
        }
      }
    }

    case GetAllQuery => sender() ! GetAllResponse(data.keySet.map(pt => GetAllSpecialtyResponseContainer(pt.id, pt.name)).toSeq)

    case GetQuery(id) => data.keySet.find(_.id == id) match {
      case Some(pet) => sender() ! FoundGetQueryResponse(pet.id, pet.name)
      case None => sender() ! NotFoundGetQueryResponse(id)
    }
  }

}
