package petclinic.api.model

import java.time.LocalDateTime

import play.api.libs.json._

case class PetOwner(id: String, name: String)
object PetOwner {
  implicit val format: OFormat[PetOwner] = Json.format[PetOwner]
}

case class Pet (birthDate: LocalDateTime, name: String, owner: PetOwner, `type`: PetType)

trait PetTypeJsonSupport {
  val petTypeReads = Reads[PetType] { json =>
    val id = (json \ "id").as[String]
    val name = (json \ "name").as[String]
    JsSuccess(new PetType(id, name))
  }

  val petTypeWrites = Writes[PetType] { petType =>
    JsObject.apply(Seq(("id", JsString(petType.getId)), ("name", JsString(petType.getName))))
  }
  implicit val petTypeFormat: Format[PetType] = Format[PetType](petTypeReads, petTypeWrites)

}

object Pet extends PetTypeJsonSupport {

  implicit val format: OFormat[Pet] = Json.format[Pet]
}
