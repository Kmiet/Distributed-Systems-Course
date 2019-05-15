package pl.edu.agh.akka.bookshop.util;

public class Query {

    public Object metaData = null;

    public Query(Object meta) {
        this.metaData = meta;
    }

    public Query() {}

    public static class FindQuery extends Query {
        public String id;

        public FindQuery(String id, Object meta) {
            super(meta);
            this.id = id;
        }

        public FindQuery(String id) {
            this.id = id;
        }
    }
}
