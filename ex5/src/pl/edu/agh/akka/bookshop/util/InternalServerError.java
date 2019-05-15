package pl.edu.agh.akka.bookshop.util;

public class InternalServerError extends Exception {

    public Object id;

    public InternalServerError(Object id) {
        this.id = id;
    }
}
