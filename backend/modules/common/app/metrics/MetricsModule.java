package metrics;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.google.inject.AbstractModule;
import play.libs.Json;
import play.libs.akka.AkkaGuiceSupport;

public class MetricsModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        bind(MetricRegistry.class).toProvider(MetricRegistryProvider.class).asEagerSingleton();
    }
}
