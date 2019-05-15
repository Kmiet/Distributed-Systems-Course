package pl.edu.agh.akka.bookshop.server;

import akka.actor.ActorPath;

public class OrderData {

    private static short instanceCounter = 1;

    public ActorPath clientPath;
    public short instancesChecked;

    public OrderData(ActorPath senderPath) {
        this.clientPath = senderPath;
        this.instancesChecked = 0;
    }

    public static void setInstanceCounter(short count) {
        instanceCounter = count;
    }

    public boolean checkedAllInstances() {
        return instancesChecked == instanceCounter ? true : false;
    }
}
