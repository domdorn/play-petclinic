package api.owners.createOwner;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CreateOwnerController extends Controller {

    @Inject
    CreateOwnerService ownerService;

    @Inject
    HttpExecutionContext ec;


    public CompletionStage<Result> createOwner() throws Exception {
        JsonNode json = request().body().asJson();
        System.out.println("json = " + json);
        OwnerCreateJava c = Json.mapper().readValue(json.toString(), OwnerCreateJava.class);
        System.out.println("c = " + c);

        final OwnerCreateJava resource = Json.fromJson(json, OwnerCreateJava.class);

        return ownerService.create(resource.firstName(), resource.lastName(), resource.address(), resource.city(), resource.telephone())
                .handle((savedResource, error) -> {
                    if (error != null) {
                        return Results.badRequest();
                    } else {
                        return Results.created(Json.toJson(savedResource));
                    }
                });
    }

}