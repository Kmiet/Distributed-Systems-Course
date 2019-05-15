package pl.edu.agh.akka.bookshop.client;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.akka.bookshop.util.BookNotFoundError;

import java.time.Duration;


public class BookshopService extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    context().child("orderingWorker").get().tell(s, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    // optional
    @Override
    public void preStart() {
        context().actorOf(Props.create(OrderingWorker.class), "orderingWorker");
    }

    private static SupervisorStrategy strategy = new OneForOneStrategy(
            10,
            Duration.ofMinutes(1),
            DeciderBuilder
                .match(BookNotFoundError.class, e -> resume())
                .matchAny(o -> restart())
                .build()
    );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

}
