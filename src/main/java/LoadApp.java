import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.CompletionStage;

import java.io.IOException;
import java.util.Queue;

public class LoadApp {

    private static final String URL_PARAM = "testUrl";
    private static final String COUNT_PARAM = "count";
    private static final Integer ASYNC_COUNT = 5;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer =
                ActorMaterializer.create(system);
        ActorRef cacheActor = system.actorOf(Props.create(CacheActor.class));
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = getFlow(cacheActor, http, system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    public static Flow<HttpRequest, HttpResponse, NotUsed> getFlow(ActorRef cacheActor, Http http, ActorSystem system, ActorMaterializer materializer) {
        return Flow
                .of(HttpRequest.class)
                .map((request) -> {
                    Query queue = request.getUri().query();
                    String url = queue.get(URL_PARAM).get();
                    int count = Integer.parseInt(queue.get(COUNT_PARAM).get());
                    return new Pair(url, count);
                })
                .mapAsync(ASYNC_COUNT, (pair) -> {
                    Patterns.ask(cacheActor, pair.first(), TIMEOUT)
                            .thenCompose((result) -> {
                                if (result != null) return CompletableFuture.completedFuture(result);
                                ping(pair);
                            });
                    return new Response();
                })
                .map((result) -> {
                    cacheActor.tell(result, ActorRef.noSender());
                    return HttpResponse.create().withEntity(
                            HttpEntities.create("URL: " + result.getUrl() + " RESPONSE TIME: " + result.getTime())
                    )
                });
    }

    pubic 
}
