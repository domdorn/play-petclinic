package api;

import akka.util.Timeout;

import java.util.concurrent.TimeUnit;

public class Constants {
     public static final Timeout TIMEOUT = Timeout.apply(500, TimeUnit.MILLISECONDS);
}
