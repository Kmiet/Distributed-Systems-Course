package pl.edu.agh.akka.bookshop.util;

public class BookNotFoundError extends Exception {

    public BookNotFoundError() {
        super("Book not found");
    }
}
