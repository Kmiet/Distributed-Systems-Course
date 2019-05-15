package pl.edu.agh.akka.bookshop.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.akka.bookshop.util.BookNotFoundError;
import pl.edu.agh.akka.bookshop.util.InternalServerError;
import pl.edu.agh.akka.bookshop.util.Query.*;
import pl.edu.agh.akka.bookshop.util.QueryResponse.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseShard extends AbstractActor {

    private String shardPath;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public DatabaseShard(String path) {
        this.shardPath = path;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FindQuery.class, q -> {
                    try {
                        String record = find(q.id.toUpperCase());
                        context().sender().tell(new FindQueryResponse(record, q.metaData), null);
                    } catch (BookNotFoundError e) {
                        context().sender().tell(new FindQueryResponse(null, q.metaData), null);
                    } catch (Exception e) {
                        context().sender().tell(new InternalServerError(q.metaData), null);
                        throw e;
                    }
                })
                .match(String.class, s -> {
                    try {
                        String record = find(s.toUpperCase());
                        context().sender().tell(record, null);
                    } catch (BookNotFoundError e) {
                        context().sender().tell(e, null);
                    }
                })
                .build();
    }

    private String find(String id) throws BookNotFoundError, NullPointerException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(shardPath));
        String line = reader.readLine();
        while(line != null) {
            if(line.startsWith(id)) {
                reader.close();
                return line;
            } else {
                line = reader.readLine();
            }
        }
        reader.close();
        throw new BookNotFoundError();
    }
}
