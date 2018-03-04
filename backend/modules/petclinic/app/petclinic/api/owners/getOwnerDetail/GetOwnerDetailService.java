package petclinic.api.owners.getOwnerDetail;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import petclinic.model.Owner;
import petclinic.model.OwnerMessages;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static petclinic.api.Constants.TIMEOUT;

public class GetOwnerDetailService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    public CompletionStage<OwnerGetDetailApiResponse> getOwner(String ownerId) {
        return PatternsCS.ask(owner, OwnerMessages.getOwnerDetails(ownerId), TIMEOUT)
                .thenApply(obj -> (OwnerMessages.GetOwnerDetailsResponse) obj).thenApply(GetOwnerDetailService::mapGetOwnerDetailsResponse);
    }

    static OwnerGetDetailApiResponse mapGetOwnerDetailsResponse(OwnerMessages.GetOwnerDetailsResponse resp) {
        return OwnerGetDetailApiResponse.apply(resp.id(), resp.firstName(), resp.lastName(), resp.address(), resp.city(), resp.telephone(), mapPetsDetailed(resp.pets(), resp.id()));
    }

    static Seq<OwnerGetDetailedPet> mapPetsDetailed(Seq<Owner.Pet> rs, String ownerId) {
        return JavaConverters.asScalaBuffer(JavaConverters.asJavaCollection(rs)
                .stream()
                .map(s -> OwnerGetDetailedPet.apply(s.id(), s.name(), s.birthDay(), s.type(), OwnerGetDetailedPetOwner.apply(ownerId)))
                .collect(Collectors.toList()));
    }
}
