package pl.edu.agh.akka.bookshop.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.akka.bookshop.util.Query;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static akka.actor.SupervisorStrategy.restart;

public class DatabaseService extends AbstractActor {

    private final List<String> worker_names = new ArrayList<>();
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Query.FindQuery.class, q -> {
                    for(String name : worker_names) {
                        context().child(name).get().forward(q, context());
                    }
                })
                .matchAny(o -> {
                    for(String name : worker_names) {
                        context().child(name).get().forward(o, context());
                    }
                })
                .build();
    }

    @Override
    public void preStart() {
        File db_folder = new File("db/");
        short instanceCount = 0;
        for(File db : db_folder.listFiles()) {
            String name = db.getName();
            if(db.isFile() && name.startsWith("books_")) {
                instanceCount += 1;
                worker_names.add(name);
                context().actorOf(Props.create(DatabaseShard.class, db.getPath()), name);
            }
        }
        OrderData.setInstanceCounter(instanceCount);
    }

    private static SupervisorStrategy strategy = new OneForOneStrategy(
            10,
            Duration.ofMinutes(1),
            DeciderBuilder.
                    matchAny(o -> restart()).
                    build()
    );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

}
