package pl.edu.agh.jgroups;

import org.jgroups.*;
import org.jgroups.util.Util;
import pl.edu.agh.jgroups.hashmap.DistributedMap;
import pl.edu.agh.jgroups.InternalMessage.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

public class Receiver extends ReceiverAdapter {

    private final JChannel channel;
    private final DistributedMap map;

    public Receiver(JChannel channel, DistributedMap store) {
        super();
        this.channel = channel;
        this.map = store;
    }

    @Override
    public void receive(Message msg) {
        InternalMessage inMsg = (InternalMessage) msg.getObject();
        switch (inMsg.type) {
            case PUT:
                handlePutMessage(inMsg);
                break;
            case REMOVE:
                handleRemoveMessage(inMsg);
                break;
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized(map) {
            Util.objectToStream(map.snapshot(), new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        HashMap<String, Integer> snapshot = (HashMap<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        Set<String> currentKeys = map.getKeySet();
        Set<String> snapshotKeys = snapshot.keySet();
        synchronized(map) {
            for(String key : currentKeys) {
                map.remove(key);
            }
            for(String key : snapshotKeys) {
                map.put(key, snapshot.get(key));
            }
        }
    }

    @Override
    public void viewAccepted(View view) {

        if(!(view instanceof MergeView)) return;

        try {
            channel.getState(null, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePutMessage(InternalMessage inMsg) {
        synchronized (map) {
            PutMessage putMsg = (PutMessage) inMsg;
            map.put(putMsg.key, putMsg.value);
        }
    }

    private void handleRemoveMessage(InternalMessage inMsg) {
        synchronized (map) {
            RemoveMessage rmMsg = (RemoveMessage) inMsg;
            map.remove(rmMsg.key);
        }
    }
}
