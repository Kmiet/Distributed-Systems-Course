package pl.edu.agh.jgroups;

import java.io.Serializable;

public class InternalMessage implements Serializable {

    protected MessageType type;

    public InternalMessage(MessageType type) {
        this.type = type;
    }

    public static class RemoveMessage extends InternalMessage {
        protected final String key;

        public RemoveMessage(String key) {
            super(MessageType.REMOVE);
            this.key = key;
        }
    }

    public static class PutMessage extends InternalMessage {
        protected final String key;
        protected final Integer value;

        public PutMessage(String key, Integer value) {
            super(MessageType.PUT);
            this.key = key;
            this.value = value;
        }
    }
}
