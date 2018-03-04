package petclinic.api.pets.getPetDetail;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import petclinic.api.model.OwnerId;
import petclinic.api.model.PetType;
import petclinic.api.pettype.getAllPetTypes.GetAllPetTypeService;
import petclinic.model.OwnerMessages;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static petclinic.api.Constants.TIMEOUT;

public class GetPetDetailService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    @Inject
    private GetAllPetTypeService petTypeService;

    public CompletionStage<Optional<GetPetDetailApiResponse>> getPetDetail(OwnerId ownerId, String petId) {
        CompletionStage<Collection<PetType>> types = petTypeService.getAll();
        final CompletionStage<OwnerMessages.GetPetDetailsResponse> getPetDetailsResponse = PatternsCS.ask(owner, OwnerMessages.getPetDetail(ownerId.id(), petId), TIMEOUT)
                .thenApply(obj -> (OwnerMessages.GetPetDetailsResponse) obj);

        return types.thenCombine(getPetDetailsResponse, (petTypes, petDetails) -> {
            if (petDetails instanceof OwnerMessages.OwnerNotFound) {
                return Optional.empty();
            } else if (petDetails instanceof OwnerMessages.PetNotFound) {
                return Optional.empty();
            } else if (petDetails instanceof OwnerMessages.PetDetailsResponse) {
                OwnerMessages.PetDetailsResponse data = (OwnerMessages.PetDetailsResponse) petDetails;

                return Optional.of(GetPetDetailApiResponse.apply(
                        data.petId(),
                        data.petName(),
                        data.petBirthDay(),
                        petTypes.stream().filter(pt -> pt.getId().equals(data.petTypeId())).findFirst().get(), // if the type does not exist, we'll blow up.. should not happen anyway!
                        GetPetDetailApiResponseOwner.apply(data.ownerId(), data.ownerFirstName(), data.ownerLastName())
                ));
            } else {
                throw new IllegalStateException("should not happen");
            }
        });
    }


}
