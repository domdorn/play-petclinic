package petclinic.api.vets.getVetDetail

case class GetVetDetailSpecialty(id: String)
case class GetVetDetailApiResponse(id: String, firstName: String, lastName: String, specialties: Seq[GetVetDetailSpecialty])