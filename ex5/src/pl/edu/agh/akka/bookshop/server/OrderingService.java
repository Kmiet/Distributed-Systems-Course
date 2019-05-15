package pl.edu.agh.akka.bookshop.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.akka.bookshop.util.BookNotFoundError;
import pl.edu.agh.akka.bookshop.util.InternalServerError;
import pl.edu.agh.akka.bookshop.util.OrderResponse;
import pl.edu.agh.akka.bookshop.util.Query.*;
import pl.edu.agh.akka.bookshop.util.QueryResponse.*;
import pl.edu.agh.akka.bookshop.util.UnknownMessageError;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class OrderingService extends AbstractActor {

    private String ordersDbPath;
    private HashMap<UUID, OrderData> currentOrders;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public OrderingService(String path) {
        this.ordersDbPath = path;
        this.currentOrders = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InternalServerError.class, e -> {
                    OrderData orderData = currentOrders.get(e.id);
                    if(orderData != null) {
                        currentOrders.remove(e.id);
                        context().actorSelection(orderData.clientPath).tell(e, null);
                    }
                })
                .match(FindQueryResponse.class, res -> {
                    UUID orderID = (UUID) res.metaData;
                    OrderData orderData = currentOrders.get(orderID);
                    if (orderData != null && res.data != null) {
                        try {
                            placeOrder(orderID, res.data);
                            String[] split = res.data.split(" ");
                            context().actorSelection(orderData.clientPath).tell(new OrderResponse(orderID, split[0], Double.parseDouble(split[1])), null);
                        } catch (IOException e) {
                            context().actorSelection(orderData.clientPath).tell(new InternalServerError(orderID), null);
                        }
                        currentOrders.remove(orderID);
                    } else if (orderData != null) {
                        orderData.instancesChecked += 1;
                        if (orderData.checkedAllInstances()) {
                            currentOrders.remove(orderID);
                            context().actorSelection(orderData.clientPath).tell(new BookNotFoundError(), null);
                        }
                    }
                })
                .match(String.class, s -> {
                    UUID key = UUID.randomUUID();
                    currentOrders.put(key, new OrderData(context().sender().path()));
                    context().system().actorSelection("user/database_supervisor").tell(new FindQuery(s, key), self());
                })
                .matchAny(o -> {
                    context().sender().tell(new UnknownMessageError(), null);
                })
                .build();
    }

    private void placeOrder(UUID id, String orderDetails) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ordersDbPath, true));
        writer.append(id + " " + orderDetails);
        writer.append(System.lineSeparator());
        writer.close();
    }
}
