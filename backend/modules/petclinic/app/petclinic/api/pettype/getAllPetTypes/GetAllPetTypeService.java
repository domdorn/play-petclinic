package petclinic.api.pettype.getAllPetTypes;

import akka.actor.ActorRef;
import petclinic.api.Constants;
import petclinic.api.model.PetType;
import petclinic.model.pettypes.PetTypeProtocol;
import scala.collection.JavaConverters;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GetAllPetTypeService {


    @Inject
    @Named("pettypes")
    private ActorRef petTypesActor;


    public CompletionStage<Collection<PetType>> getAll() {
        return akka.pattern.PatternsCS.ask(petTypesActor, PetTypeProtocol.getAll(), Constants.TIMEOUT)
                .thenApply(obj -> (PetTypeProtocol.GetAllResponse) obj)
                .thenApply(resp -> JavaConverters
                        .asJavaCollection(resp.types())
                        .stream()
                        .map(o -> new PetType(o.id(), o.name()))
                        .collect(Collectors.toList())
                );
    }

}
