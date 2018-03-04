package petclinic;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.google.inject.AbstractModule;
import metrics.MetricRegistryProvider;
import petclinic.model.OwnerRepository;
import petclinic.model.pettypes.PetTypeEntity;
import petclinic.v1.post.JPAPostRepository;
import petclinic.v1.post.PostRepository;
import play.libs.Json;
import play.libs.akka.AkkaGuiceSupport;

public class PetClinicModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        bind(MetricRegistry.class).toProvider(MetricRegistryProvider.class).asEagerSingleton();
        bind(PostRepository.class).to(JPAPostRepository.class).asEagerSingleton();

        Json.mapper().registerModule(new DefaultScalaModule());

        bindActor(OwnerRepository.class, "owner");
        bindActor(PetTypeEntity.class, "pettypes");

    }
}
