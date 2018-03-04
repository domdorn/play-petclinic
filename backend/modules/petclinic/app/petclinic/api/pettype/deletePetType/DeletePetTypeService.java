package petclinic.api.pettype.deletePetType;

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

public class DeletePetTypeService {

    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;


    public CompletionStage<Optional<Boolean>> deleteType(String id) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.delete(id), Constants.TIMEOUT)
                .thenApply(o -> (PetTypeProtocol.PetTypeDeleteResponse) o)
                .thenApply(o -> {
                    if (o instanceof PetTypeProtocol.PetTypeDeletedResponse$) {
                        return Optional.of(true);
                    } else if (o instanceof PetTypeProtocol.PetTypeStillInUseResponse$) {
                        return Optional.of(false);
                    } else if (o instanceof PetTypeProtocol.PetTypeToDeleteNotFoundResponse$) {
                        return Optional.empty();
                    } else {
                        throw new IllegalStateException("unhandled response received: " + o);
                    }
                });
    }
}
