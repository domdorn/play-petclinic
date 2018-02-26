package api.pettype;

import akka.actor.ActorRef;
import akka.util.Timeout;
import api.model.PetType;
import model.pettype.PetTypeProtocol;
import scala.collection.JavaConverters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PetTypeService {

    private static final Timeout TIMEOUT = Timeout.apply(500, TimeUnit.MILLISECONDS);

    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;


    public CompletionStage<Collection<PetType>> getAll() {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.getAll(), TIMEOUT)
                .thenApply(obj -> (PetTypeProtocol.GetAllResponse) obj)
                .thenApply(resp -> JavaConverters
                        .asJavaCollection(resp.types())
                        .stream()
                        .map(o -> new PetType(o.id(), o.name()))
                        .collect(Collectors.toList())
                );
    }

    public CompletionStage<Optional<PetType>> getPetType(String id) {
        final PetTypeProtocol.GetQuery message = model.pettype.PetTypeProtocol.get(id);
        return akka.pattern.PatternsCS.ask(petTypesActor, message, TIMEOUT)
                .thenApply(obj -> (PetTypeProtocol.GetQueryResponse) obj)
                .thenApply(resp -> {
                    if (resp instanceof PetTypeProtocol.NotFoundGetQueryResponse) {
                        return Optional.empty();
                    } else if (resp instanceof PetTypeProtocol.FoundGetQueryResponse) {
                        final PetTypeProtocol.FoundGetQueryResponse resp1 = (PetTypeProtocol.FoundGetQueryResponse) resp;
                        return Optional.of(new PetType(resp1.id(), resp1.name()));
                    } else {
                        throw new IllegalStateException("unkown response type");
                    }
                });
    }

    public CompletionStage<Optional<Boolean>> deleteType(String id) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.delete(id), TIMEOUT)
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

    public CompletionStage<Optional<PetType>> changeName(String id, String name) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.changeName(id, name), TIMEOUT)
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

    public CompletionStage<PetType> create(String name) {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.createPetType(name), TIMEOUT)
                .thenApply(obj -> (PetTypeProtocol.PetTypeCreateResponse) obj).thenApply(r -> new PetType(r.id(), r.name()));
    }
}
