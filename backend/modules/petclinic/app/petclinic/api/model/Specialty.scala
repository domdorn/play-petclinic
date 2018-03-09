package petclinic.api.model

import play.api.libs.json.Json

case class Specialty (id: String, name: String)

object Specialty {
  implicit val format = Json.format[Specialty]
}
