package pl.edu.agh.akka.bookshop.client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.akka.bookshop.util.BookNotFoundError;
import pl.edu.agh.akka.bookshop.util.InternalServerError;
import pl.edu.agh.akka.bookshop.util.OrderResponse;

public class OrderingWorker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(InternalServerError.class, e -> {
                    log.error(e.toString());
                })
                .match(BookNotFoundError.class, e -> {
                    log.error(e.toString());
                })
                .match(OrderResponse.class, res -> {
                    log.info(res.toString());
                })
                .match(String.class, s -> {
                    context().actorSelection("akka.tcp://bookshop_server@127.0.0.1:2552/user/ordering_service").tell(s, self());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
