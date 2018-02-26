package api.owners.createOwner;

import akka.actor.ActorRef;
import akka.pattern.AskSupport;
import akka.pattern.PatternsCS;
import api.model.OwnerId;
import model.OwnerMessages;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

import static api.Constants.TIMEOUT;

public class CreateOwnerService implements AskSupport {

    @Inject
    @Named("owner")
    private ActorRef owner;

    public CompletionStage<OwnerId> create(String firstName, String lastName, String address, String city, String telephone) {
        return PatternsCS.ask(owner, OwnerMessages.createOwner(firstName, lastName, address, city, telephone), TIMEOUT)
                .thenApply(obj -> OwnerId.apply(obj.toString()));
    }

}
