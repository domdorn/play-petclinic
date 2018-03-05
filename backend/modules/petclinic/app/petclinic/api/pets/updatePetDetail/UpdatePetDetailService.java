package petclinic.api.pets.updatePetDetail;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import petclinic.api.model.OwnerId;
import petclinic.api.model.PetType;
import petclinic.api.pettype.getPetType.GetPetTypeService;
import petclinic.model.OwnerMessages;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static petclinic.api.Constants.TIMEOUT;

public class UpdatePetDetailService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    @Inject
    private GetPetTypeService petTypeService;

    public CompletionStage<Optional<UpdatePetDetailApiResponse>> updatePetDetails(OwnerId ownerId, String petId, String name, LocalDate birthDay, String petTypeId) {
        return petTypeService.getPetType(petTypeId).thenCompose(maybePetType -> {
            if (!maybePetType.isPresent()) {
                return CompletableFuture.completedFuture(Optional.<UpdatePetDetailApiResponse>empty());
            } else {
                PetType petType = maybePetType.get();
                return PatternsCS.ask(owner, OwnerMessages.updatePetDetails(ownerId.id(), petId, name, birthDay, petType), TIMEOUT)
                        .thenApply(obj -> (OwnerMessages.UpdatePetDetailsResponse) obj)
                        .thenApply(obj -> {
                            if (obj instanceof OwnerMessages.OwnerNotFound) {
                                return Optional.empty();
                            } else if (obj instanceof OwnerMessages.PetNotFound) {
                                return Optional.empty();
                            } else if (obj instanceof OwnerMessages.PetDetailsUpdatedResponse) {
                                return Optional.of(UpdatePetDetailApiResponse.apply(((OwnerMessages.PetDetailsUpdatedResponse) obj).petId()));
                            } else {
                                throw new IllegalStateException("unsupported state");
                            }
                        });
            }
        });
    }

}