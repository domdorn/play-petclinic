package api.owners.getAllOwners;

import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetAllOwnersController extends Controller {

    @Inject
    GetAllOwnersService ownerService;

    @Inject
    HttpExecutionContext ec;


    public CompletionStage<Result> getOwners() {
        // returns a Collection<Owner>
        return ownerService.getOwners()
                .thenApplyAsync(owners -> Json.toJson(owners), ec.current())
                .thenApply(json -> Results.ok(json));
    }

}