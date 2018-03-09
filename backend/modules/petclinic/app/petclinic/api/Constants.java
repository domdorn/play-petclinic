package petclinic.api;

import akka.util.Timeout;

import java.util.concurrent.TimeUnit;

public class Constants {
     public static final Timeout TIMEOUT = Timeout.apply(1500, TimeUnit.MILLISECONDS);

     public static final String OWNER_AGGREGATE_ACTOR_NAME = "owners";
     public static final String VETS_AGGREGATE_ACTOR_NAME = "vets";
     public static final String PET_TYPES_ACTOR_NAME = "pettypes";
     public static final String SPECIALTIES_ACTOR_NAME = "specialties";
}
