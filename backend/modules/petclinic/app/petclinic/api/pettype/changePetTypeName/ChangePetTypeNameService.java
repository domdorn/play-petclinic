package petclinic.api.pettype.changePetTypeName;

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

public class ChangePetTypeNameService {
    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;

    public CompletionStage<Optional<PetType>> changeName(String id, String name) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.changeName(id, name), Constants.TIMEOUT)
                .thenApply(o -> (PetTypeProtocol.PetTypeNameChangeResponse) o)
                .thenApply(o -> {
                    if (o instanceof PetTypeProtocol.PetTypeNameChangedResponse) {
                        final PetTypeProtocol.PetTypeNameChangedResponse o1 = (PetTypeProtocol.PetTypeNameChangedResponse) o;
                        return Optional.of(new PetType(o1.id(), o1.name()));
                    } else if (o instanceof PetTypeProtocol.PetTypeNameConflictResponse$) {
                        throw new IllegalArgumentException("pettype name already in use");
                    } else if (o instanceof PetTypeProtocol.PetTypeToChangeNotFoundResponse$) {
                        return Optional.empty();
                    } else {
                        throw new IllegalStateException("unhandled response received: " + o);
                    }
                });
    }
}
