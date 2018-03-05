package petclinic.model.vets

object Protocol {

  sealed trait Command
  sealed trait Query
  sealed trait Response

  // use case 1: get all vets
  case object GetAllVets extends Query
  case class BasicVet(id: String, name: String)
  case class GetAllVetsReponse(vets: Seq[BasicVet]) extends Response


  // use case 2: create a vet
  case class CreateVetCommand(name: String) extends Command
  sealed trait CreateVetResponse extends Response
  case class VetCreatedResponse(id: String) extends CreateVetResponse
  case object VetCouldNotBeCreatedResponse extends CreateVetResponse

  // use case 3: update a vet
  case class UpdateVetCommand(id: String, name: String) extends Command
  sealed trait UpdateVetResponse extends Response
  case object VetUpdatedResponse extends UpdateVetResponse
  case object VetUpdateFailedResponse extends UpdateVetResponse


  // use case 4: Get Details of a vet
  case class GetVetDetailsQuery(id: String) extends Query

  sealed trait GetVetDetailsResponse extends Response

  case class VetDetails(id: String, name: String) extends GetVetDetailsResponse

  case object VetNotFound extends GetVetDetailsResponse


}
