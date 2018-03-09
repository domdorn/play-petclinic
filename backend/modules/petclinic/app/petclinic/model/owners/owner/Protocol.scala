package petclinic.model.owners.owner

import java.time.LocalDate

import akka.actor.ActorRef
import petclinic.api.model.PetType
import petclinic.model.owners.Owner.Pet

object Protocol {
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
  sealed trait OverviewStateResponse {
    val data: Seq[OwnerOverviewStateResponse]
  }
  case class OverviewStateResponseComplete(data: Seq[OwnerOverviewStateResponse]) extends OverviewStateResponse
  case class OverviewStateResponseIncomplete(data: Seq[OwnerOverviewStateResponse], missing: Seq[ActorRef]) extends OverviewStateResponse
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
