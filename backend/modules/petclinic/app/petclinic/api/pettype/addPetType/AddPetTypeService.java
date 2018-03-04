package petclinic.api.pettype.addPetType;

import akka.actor.ActorRef;
import akka.util.Timeout;
import petclinic.api.Constants;
import petclinic.api.model.PetType;
import petclinic.model.pettypes.PetTypeProtocol;
import scala.collection.JavaConverters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AddPetTypeService {

    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;

    public CompletionStage<PetType> create(String name) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.createPetType(name), Constants.TIMEOUT)
                .thenApply(obj -> (PetTypeProtocol.PetTypeCreateResponse) obj).thenApply(r -> new PetType(r.id(), r.name()));
    }
}
