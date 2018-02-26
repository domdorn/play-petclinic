package model

import java.time.LocalDate

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Props}
import akka.pattern.{AskSupport, PipeToSupport}
import akka.persistence.PersistentActor
import akka.util.Timeout
import api.model.PetType
import model.Owner.Pet
import model.OwnerRepository.OwnerIdCreatedEvent
import model.OwnerEvents._
import model.OwnerMessages._
import tyrex.services.UUID

import scala.collection.parallel.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object OwnerMessages {

  sealed trait Command

  sealed trait Query

  sealed trait Response

  sealed trait ForwardToOwnerMessage{ val ownerId: String }
  sealed trait OwnerNotFound

  //<editor-fold desc="use case one: create a new owner">
  // use case one: create a new owner
  case class CreateOwnerCommand(firstName: String, lastName: String, address: String, city: String, telephone: String) extends Command

  def createOwner(firstName: String, lastName: String, address: String, city: String, telephone: String) = CreateOwnerCommand(firstName: String, lastName: String, address: String, city: String, telephone: String)
  //</editor-fold>


  //<editor-fold desc="use case two: Query all owners for the overview page">
  // use case two: query all owners for the overview page
  // message to the aggregate
  def getAllOwners: GetAllOwnersQuery = GetAllOwnersQuery()
  case class GetAllOwnersQuery() extends Query
  // query to the to the owner-actors
  case object OverviewStateQuery extends Query
  // response from the owner actors
  case class OwnerOverviewStateResponse(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[String]) extends Response
  // response from the aggregate
  case class OverviewStateResponse(data: Seq[OwnerOverviewStateResponse])
  //</editor-fold>



  //region use case three: get the details of one owner
  def getOwnerDetails(uuid: String) = GetOwnerDetailsQuery(uuid)
  case class GetOwnerDetailsQuery(ownerId: String) extends Query with ForwardToOwnerMessage
  case class GetOwnerDetailsResponse(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[Pet]) extends Response
  //endregion

  sealed trait PetNotFound

  //region use case four: add a pet to a owner
  def addPet(ownerId: String, name: String, birthday: LocalDate, petTypeId: String, petTypeName: String) = AddPetCommand(ownerId, name, birthday, petTypeId, petTypeName)
  case class AddPetCommand(ownerId: String, name: String, birthday: LocalDate, petTypeId: String, petTypeName: String) extends Command with ForwardToOwnerMessage
  sealed trait AddPetResponse extends Response
  case class PetAddedResponse(id: String, name: String, birthday: LocalDate, petTypeId: String) extends AddPetResponse
  case object OwnerNotFoundPetAddedResponse extends AddPetResponse with OwnerNotFound
  //endregion


  //region use case five: get the details of a pet
  case class GetPetDetailsQuery(ownerId: String, petId: String) extends Query with ForwardToOwnerMessage
  def getPetDetail(ownerId: String, petId: String) = GetPetDetailsQuery(ownerId, petId)
  sealed trait GetPetDetailsResponse extends Response
  case object OwnerNotFoundGetPetDetailsResponse extends GetPetDetailsResponse with OwnerNotFound
  case object PetNotFoundGetPetDetailsresponse extends GetPetDetailsResponse with PetNotFound
  case class PetDetailsResponse(ownerId: String, ownerFirstName: String, ownerLastName: String, petId: String, petName: String, petBirthDay: LocalDate, petTypeId: String) extends GetPetDetailsResponse
  //endregion

  //region use case six: update pet details
  case class UpdatePetDetailsCommand(ownerId: String, petId: String, name: String, birthday: LocalDate, petType: PetType) extends Command with ForwardToOwnerMessage
  def updatePetDetails(ownerId: String, petId: String, name: String, birthday: LocalDate, petType: PetType) = UpdatePetDetailsCommand(ownerId, petId, name, birthday, petType)
  sealed trait UpdatePetDetailsResponse extends Response
  case object OwnerNotFoundUpdatePetDetailsResponse extends UpdatePetDetailsResponse with OwnerNotFound
  case object PetNotFoundUpdatePetDetailsResponse extends UpdatePetDetailsResponse with PetNotFound
  case class PetDetailsUpdatedResponse(petId: String) extends UpdatePetDetailsResponse
  //endregion

}

object OwnerRepository {
  private[model] case class OwnerIdCreatedEvent(uuid: String)
}

class OwnerRepository extends PersistentActor with AskSupport with PipeToSupport {
  import scala.language.postfixOps
  implicit val timeout: Timeout = Timeout(1 second)
  implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId: String = "owners-aggregate"

  var childrenIds: Seq[String] = Nil

  override def receiveRecover: Receive = {
    case OwnerIdCreatedEvent(uuid) => {
      println("recovering children, adding id: " + uuid)
      childrenIds = childrenIds ++ Seq(uuid)
      println("childrenIds is now " + childrenIds)
    }
    case x => println(s"trying to recover message $x")
  }

  def getOrCreateChild(id:String): ActorRef = context.child(id).getOrElse(context.actorOf(Owner.create(id), id))

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

      val questionsToMyChildren: Iterable[Future[OwnerOverviewStateResponse]] = childrenIds
        .map(id => getOrCreateChild(id))
        .map(child => (child ? OverviewStateQuery).mapTo[OwnerOverviewStateResponse])

      val futureResponses = Future.sequence(questionsToMyChildren) map (resp => OverviewStateResponse(resp.toSeq))
      pipe(futureResponses).to(_sender)
    }

  }
}

object Owner {
  def create(uuid: String) = Props(classOf[Owner], uuid)

  case class Pet(id: String, name: String, birthDay: LocalDate, `type`: String)

  case class State(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String, pets: Seq[Pet]) {
    def updatePet(newPet: Pet): State = {
      copy(pets = pets.filterNot(_.id == newPet.id) ++ Seq(newPet))
    }
  }

  case object GetOverviewState

}
object OwnerEvents {

  sealed trait Event

  case class OwnerCreatedEvent(id: String, firstName: String, lastName: String, address: String, city: String, telephone: String) extends Event
  case class PetAddedEvent(id: String, name: String, birthDay: LocalDate, petTypeId: String) extends Event

  sealed trait PetChangedEvent extends Event {
    val id: String
  }
  case class PetNameChangedEvent(id: String, name: String) extends PetChangedEvent
  case class PetBirthdayChangedEvent(id: String, birthDay: LocalDate) extends PetChangedEvent
  case class PetTypeChangedEvent(id: String, newTypeId: String) extends PetChangedEvent
}

class Owner(uuid: String) extends PersistentActor {
  override def persistenceId: String = s"owner-$uuid"

  var state: Owner.State = _
  var lastPetId = 0

  override def receiveRecover: Receive = {
    case x: OwnerCreatedEvent =>       state = Owner.State(x.id, x.firstName, x.lastName, x.address, x.city, x.telephone, Nil)

    case x: PetAddedEvent => {
      state = state.copy(pets = state.pets ++ Seq(Owner.Pet(x.id, x.name, x.birthDay, x.petTypeId)))
      lastPetId = lastPetId + 1
    }

    case x: PetChangedEvent => {
      val pet = state.pets.find(_.id == x.id)
      pet match {
        case None => throw new IllegalStateException("this may not happen")
        case Some(p) => x match {
          case PetNameChangedEvent(id, name) => state = state.updatePet(p.copy(name = name))
          case PetBirthdayChangedEvent(id, birthDay) => state = state.updatePet(p.copy(birthDay = birthDay))
          case PetTypeChangedEvent(id, newTypeId) => state = state.updatePet(p.copy(`type` = newTypeId))
        }
      }

    }
  }

  override def receiveCommand: Receive = {

    case x: CreateOwnerCommand => {
      persist(OwnerCreatedEvent(uuid, x.firstName, x.lastName, x.address, x.city, x.telephone)) { ev =>
        receiveRecover.apply(ev)
      }
      sender ! uuid
    }
    case x: AddPetCommand if x.ownerId == uuid => {
      state.pets.find(p => p.name == x.name && p.`type` == x.petTypeId && p.birthDay == x.birthday) match {
        case Some(pet) => sender() ! PetAddedResponse(pet.id, pet.name, pet.birthDay, pet.`type`)
        case None => persist(PetAddedEvent((lastPetId+1).toString, x.name, x.birthday, x.petTypeId)){ ev =>
          receiveRecover(ev)
          sender() ! PetAddedResponse(ev.id, ev.name, ev.birthDay, ev.petTypeId)
        }
      }
    }

    case x: UpdatePetDetailsCommand => state.pets.find(_.id == x.petId) match {
      case None => sender() ! PetNotFoundUpdatePetDetailsResponse
      case Some(pet) => {
        val empty = scala.collection.immutable.Nil

        val changeEvents: scala.collection.immutable.Seq[PetChangedEvent] = empty ++
          (if(pet.name != x.name) Seq(PetNameChangedEvent(pet.id, x.name)) else empty) ++
          (if(!pet.birthDay.isEqual(x.birthday)) Seq(PetBirthdayChangedEvent(pet.id, x.birthday)) else empty) ++
          (if(pet.`type` != x.petType.getId) Seq(PetTypeChangedEvent(pet.id, x.petType.getId)) else empty)


        persistAll[PetChangedEvent](changeEvents) {
          event =>
            receiveRecover(event)
        }
        sender() ! PetDetailsUpdatedResponse(pet.id)
      }
    }

    case OverviewStateQuery                     => sender ! OwnerOverviewStateResponse(state.id, state.firstName, state.lastName, state.address, state.city, state.telephone, state.pets.map(_.name))
    case GetOwnerDetailsQuery(id) if id == uuid => sender() ! GetOwnerDetailsResponse(uuid, state.firstName, state.lastName, state.address, state.city, state.telephone, state.pets)
    case GetOwnerDetailsQuery(id)               => Failure(new IllegalArgumentException("should not have received this id"))

    case GetPetDetailsQuery(ownerId, petId)     => state.pets.find(_.id == petId) match {
      case Some(pet) => sender() ! PetDetailsResponse(ownerId: String, state.firstName, state.lastName, pet.id, pet.name, pet.birthDay, pet.`type`)
      case None => sender() ! PetNotFoundGetPetDetailsresponse
    }

  }

}


