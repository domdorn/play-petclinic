package api.owners.getAllOwners

import java.util

import scala.beans.BeanProperty

// objects received when querying all owners
case class OwnerGetPet(@BeanProperty name: String)
case class OwnerGetApiResponse(@BeanProperty id: String, @BeanProperty firstName: String, @BeanProperty lastName: String, @BeanProperty address: String, @BeanProperty city: String, @BeanProperty telephone: String, @BeanProperty pets: util.Collection[OwnerGetPet])
