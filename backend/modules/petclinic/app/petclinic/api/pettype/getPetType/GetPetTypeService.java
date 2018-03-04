package petclinic.api.pettype.getPetType;

import akka.actor.ActorRef;
import petclinic.api.Constants;
import petclinic.api.model.PetType;
import petclinic.model.pettypes.PetTypeProtocol;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class GetPetTypeService {


    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;


    public CompletionStage<Optional<PetType>> getPetType(String id) {
        final PetTypeProtocol.GetQuery message = PetTypeProtocol.get(id);
        return akka.pattern.PatternsCS.ask(petTypesActor, message, Constants.TIMEOUT)
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

}
