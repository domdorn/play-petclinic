package petclinic.api.owners.getAllOwners;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import petclinic.model.OwnerMessages;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static petclinic.api.Constants.TIMEOUT;

public class GetAllOwnersService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    public CompletionStage<Collection<OwnerGetApiResponse>> getOwners() {
        return PatternsCS
                .ask(owner, OwnerMessages.getAllOwners(), TIMEOUT)
                .thenApply(GetAllOwnersService::mapOverviewStateResponse);
    }

    static Collection<OwnerGetApiResponse> mapOverviewStateResponse(Object object) {
        OwnerMessages.OverviewStateResponse resp = (OwnerMessages.OverviewStateResponse) object;
        return JavaConverters.asJavaCollection(resp.data())
                .stream()
                .map(rs ->
                        OwnerGetApiResponse.apply(
                                rs.id(),
                                rs.firstName(),
                                rs.lastName(),
                                rs.address(),
                                rs.city(),
                                rs.telephone(),
                                JavaConverters.asScalaBuffer(mapPets(rs.pets()))))
                .collect(Collectors.toList());
    }

    static List<OwnerGetPet> mapPets(Seq<String> rs) {
        return JavaConverters.asJavaCollection(rs)
                .stream()
                .map(OwnerGetPet::apply)
                .collect(Collectors.toList());
    }

}
