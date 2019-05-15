package pl.edu.agh.akka.bookshop.util;

import java.io.Serializable;
import java.util.UUID;

public class OrderResponse implements Serializable {

    public UUID orderID;
    public String book;
    double price;

    public OrderResponse(UUID orderID, String title, double price) {
        this.orderID = orderID;
        this.book = title;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Placed order: " + orderID + " TITLE: " + book + " PRICE: " + price;
    }
}
