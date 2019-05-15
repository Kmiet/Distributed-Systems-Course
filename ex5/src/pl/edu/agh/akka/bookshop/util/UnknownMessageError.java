package pl.edu.agh.akka.bookshop.util;

public class UnknownMessageError extends Exception {
    public UnknownMessageError() {
        super("Unknown message type");
    }
}
