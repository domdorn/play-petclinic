package model.pettype

import akka.persistence.PersistentActor
import model.pettype.PetTypeProtocol._

object PetTypeProtocol {

  sealed trait Query
  sealed trait Command
  sealed trait Response
  private[pettype] sealed trait Event

  // use case 1: create a new pet type
  def createPetType(name:String) = CreatePetTypeCommand(name)
  case class CreatePetTypeCommand(name: String) extends Command
  sealed trait PetTypeCreateResponse extends Response {
    val id: String
    val name: String
  }
  case class PetTypeCreatedResponse(id: String, name: String) extends PetTypeCreateResponse
  case class PetTypeAlreadyExistsResponse(id: String, name: String) extends PetTypeCreateResponse
  private[pettype] case class PetTypeCreatedEvent(id: String, name: String) extends Event

  // use case 2: get all pet types
  def getAll(): GetAllQuery.type = GetAllQuery
  case object GetAllQuery extends Query
  case class GetAllResponse(types: Seq[GetAllPetType]) extends Response

  case class GetAllPetType(id: String, name: String)


  // use case 3: get a single pet type:
  sealed trait GetQueryResponse extends Response
  case class GetQuery(id: String) extends Query
  def get(id: String) = GetQuery(id)
  case class FoundGetQueryResponse(id: String, name: String) extends GetQueryResponse
  case class NotFoundGetQueryResponse(id: String) extends GetQueryResponse

  // use case 4: delete a pet type
  case class DeletePetTypeCommand(id: String) extends Command
  def delete(id: String) = DeletePetTypeCommand(id)
  sealed trait PetTypeDeleteResponse extends Response
  case object PetTypeToDeleteNotFoundResponse extends PetTypeDeleteResponse
  case object PetTypeDeletedResponse extends PetTypeDeleteResponse
  case object PetTypeStillInUseResponse extends PetTypeDeleteResponse
  private[pettype] case class PetTypeDeletedEvent(id: String, name: String)


  // use case 5: change pet type name
  case class ChangePetTypeNameCommand(id: String, newName: String) extends Command
  def changeName(id: String, newName: String) = ChangePetTypeNameCommand(id, newName)
  sealed trait PetTypeNameChangeResponse extends Response
  case object PetTypeToChangeNotFoundResponse extends PetTypeNameChangeResponse
  case object PetTypeNameConflictResponse extends PetTypeNameChangeResponse
  case class PetTypeNameChangedResponse(id: String, name: String) extends PetTypeNameChangeResponse
  private[pettype] case class PetTypeNameChangedEvent(id: String, name: String) extends Event


  private[pettype] case class PetType(id: String, name: String)
}

class PetTypeEntity extends PersistentActor {
  override def persistenceId: String = "pettypes"

  var petTypes: Map[PetType, Int] = Map()
  var latestId = 1

  override def receiveRecover: Receive = {
    case PetTypeCreatedEvent(id, name) =>
      petTypes = petTypes + (PetType(id, name) -> 0)
      latestId = latestId + 1
    case PetTypeDeletedEvent(id, name) => petTypes = petTypes - PetType(id, name)
    case PetTypeNameChangedEvent(id, newName) => {
      petTypes.find(_._1.id == id) match {
        case Some(entry) => petTypes = (petTypes - entry._1) + (PetType(id, newName) -> entry._2)
        case None => throw new IllegalStateException("this should not happen!")
      }
    }
  }

  override def receiveCommand: Receive = {
    case CreatePetTypeCommand(name) => petTypes.keySet.find(pt => pt.name == name) match {
        case Some(pet) => sender() ! PetTypeAlreadyExistsResponse(pet.id, pet.name)
        case None =>  persist(PetTypeCreatedEvent(latestId.toString, name)){ ev =>
          receiveRecover(ev)
          sender() ! PetTypeCreatedResponse(ev.id, ev.name)
        }
      }

    case DeletePetTypeCommand(id) => petTypes.keySet.find(_.id == id) match {
        case Some(pet) => if(petTypes(pet) > 0) sender() ! PetTypeStillInUseResponse else {
          val s = sender()
          persist(PetTypeDeletedEvent(pet.id, pet.name)) { ev =>
            receiveRecover(ev)
            s ! PetTypeDeletedResponse
          }
        }
        case None => sender() ! PetTypeToDeleteNotFoundResponse
      }

    case ChangePetTypeNameCommand(id, name) => petTypes.keySet.find(_.id == id) match {
      case None => sender() ! PetTypeToChangeNotFoundResponse
      case Some(pet) => if(pet.name == name) {
        sender() ! PetTypeNameChangedResponse(pet.id, pet.name)
      } else if (name.isEmpty) {
        sender() ! PetTypeNameConflictResponse
      } else {
        petTypes.keySet.find(_.name == name) match {
          case None => persist(PetTypeNameChangedEvent(id, name)){ ev =>
            receiveRecover(ev)
            sender() ! PetTypeNameChangedResponse(id, name)
            // TODO publish domain event?
          }
          case Some(existingPetWithThisName) => if(existingPetWithThisName.id == pet.id) {
            sender() ! PetTypeNameChangedResponse(id, name)
          } else {
            sender() ! PetTypeNameConflictResponse
          }
        }
      }
    }

    case GetAllQuery => sender() ! GetAllResponse(petTypes.keySet.map(pt => GetAllPetType(pt.id, pt.name)).toSeq)

    case GetQuery(id) => petTypes.keySet.find(_.id == id) match {
      case Some(pet) => sender() ! FoundGetQueryResponse(pet.id, pet.name)
      case None => sender() ! NotFoundGetQueryResponse(id)
    }
  }

}
