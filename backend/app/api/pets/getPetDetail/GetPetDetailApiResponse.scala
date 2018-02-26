package api.pets.getPetDetail

import java.time.LocalDate

import api.model.PetType

case class GetPetDetailApiResponseOwner(id: String, firstName: String, lastName: String)
case class GetPetDetailApiResponse(id: String, name: String, birthDate: LocalDate, `type`: PetType, owner: GetPetDetailApiResponseOwner)