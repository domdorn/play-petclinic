package api.pettype;

import api.model.PetType;
import com.fasterxml.jackson.databind.JsonNode;
import helpers.EX;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class PetTypeController extends Controller {

    @Inject
    PetTypeService petTypeService;


    public CompletionStage<Result> getAllPetTypes() {
        return petTypeService.getAll().thenApply(types -> Results.ok(Json.toJson(types)));
    }

    public CompletionStage<Result> addPetType() {
        JsonNode json = request().body().asJson();
        PetType petType = Json.fromJson(json, PetType.class);
        return petTypeService.create(petType.getName()).thenApply(res -> Results.ok(Json.toJson(res)));
    }

    public CompletionStage<Result> deletePetType(String id) {
        return petTypeService.deleteType(id).handle((done, error) -> {
            if (error != null) {
                return Results.badRequest(error.getMessage());
            } else {
                return done
                        .map(worked -> worked ? Results.noContent() : Results.forbidden("type still in use"))
                        .orElse(Results.notFound());
            }
        });
    }

    public CompletionStage<Result> getPetType(String id) {
        return petTypeService.getPetType(id).thenApply(obj -> obj.map(o -> Results.ok(Json.toJson(o))).orElse(Results.notFound()));
    }

    public CompletionStage<Result> changePetTypeName(String id) {
        JsonNode json = request().body().asJson();
        PetType petType = Json.fromJson(json, PetType.class);
        return petTypeService.changeName(id, petType.getName()).handle((res, err1) -> {
            if (err1 != null) {
                Throwable err = EX.unwrap(err1);
                if (err instanceof IllegalArgumentException) {
                    Map<String, String> errorData = new HashMap<>();
                    errorData.put("status", "400");
                    errorData.put("statusText", "bad request");
                    errorData.put("message", "name in use or invalid");
                    return Results.badRequest(Json.toJson(errorData));
                } else {
                    Map<String, String> errorData = new HashMap<>();
                    errorData.put("status", "500");
                    errorData.put("statusText", "internal server error happened");
                    errorData.put("message", "an error occured");
                    return Results.internalServerError(Json.toJson(errorData));
                }
            } else {
                return Results.noContent();
            }
        });
    }


}