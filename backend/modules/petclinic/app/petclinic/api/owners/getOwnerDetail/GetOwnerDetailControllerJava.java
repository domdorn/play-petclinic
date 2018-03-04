package petclinic.api.owners.getOwnerDetail;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetOwnerDetailControllerJava extends Controller {

    @Inject
    GetOwnerDetailService ownerService;

    public CompletionStage<Result> getOwner(String ownerId) {

        return ownerService.getOwner(ownerId)
                .handle((obj, err) -> {
                    if (err != null) {
                        return Results.notFound(err.getMessage());
                    } else {
                        return Results.ok(Json.toJson(obj));
                    }
                });
    }

}