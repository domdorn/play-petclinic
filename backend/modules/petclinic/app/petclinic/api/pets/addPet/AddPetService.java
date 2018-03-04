package petclinic.api.pets.addPet;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import petclinic.api.model.OwnerId;
import petclinic.api.model.PetType;
import petclinic.model.OwnerMessages;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static petclinic.api.Constants.TIMEOUT;

public class AddPetService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    public CompletionStage<Optional<AddPetApiResponse>> addPet(OwnerId ownerId, String name, LocalDate birthday, PetType type) {
        return PatternsCS.ask(owner, OwnerMessages.addPet(ownerId.id(), name, birthday, type.getId(), type.getName()), TIMEOUT)
                .thenApply(obj -> (OwnerMessages.AddPetResponse) obj).thenApply(obj -> {
                    if (obj instanceof OwnerMessages.OwnerNotFound) {
                        return Optional.empty();
                    } else {
                        OwnerMessages.PetAddedResponse data = (OwnerMessages.PetAddedResponse) obj;
                        AddPetApiResponse resp = AddPetApiResponse.apply(data.id());
                        return Optional.of(resp);
                    }
                });
    }


}
