import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    Map<String, Integer> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(String.class, url -> sender().tell(cache.get(url), ActorRef.noSender()))
                //.match(Response.class, cache.put())
                .build();
    }
}
