package petclinic.api.model

import scala.beans.BeanProperty

trait NamedEntity {
  @BeanProperty val name: String
}
