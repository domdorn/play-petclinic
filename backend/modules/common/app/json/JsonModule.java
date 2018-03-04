package json;

import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.google.inject.AbstractModule;
import play.libs.Json;
import play.libs.akka.AkkaGuiceSupport;

public class JsonModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        Json.mapper().registerModule(new DefaultScalaModule());
    }
}
