package api.model

import play.api.libs.json.Json

case class OwnerId(id: String) {
  override def toString: String = id
}

object OwnerId {
  implicit val writes = Json.writes[OwnerId]
  implicit val reads = Json.reads[OwnerId]
}

