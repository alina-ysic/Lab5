import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {
    private static final int DEFAULT_RESULT = -1;
    private Map<String, Integer> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(String.class, url -> sender().tell(cache.getOrDefault(url, DEFAULT_RESULT), ActorRef.noSender()))
                .match(Response.class, response -> cache.put(response.getUrl(), response.getTime()))
                .build();
    }
}
