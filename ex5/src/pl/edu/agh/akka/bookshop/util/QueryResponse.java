package pl.edu.agh.akka.bookshop.util;

public class QueryResponse {

    public Object metaData = null;

    public QueryResponse(Object meta) {
        this.metaData = meta;
    }

    public QueryResponse() {}

    public static class FindQueryResponse extends QueryResponse {

        public String data;

        public FindQueryResponse(String data, Object meta) {
            super(meta);
            this.data = data;
        }

        public FindQueryResponse(String result) {
            this.data = result;
        }
    }
}
