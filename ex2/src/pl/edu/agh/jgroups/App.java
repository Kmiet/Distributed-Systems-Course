package pl.edu.agh.jgroups;

import org.jgroups.JChannel;

import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.ProtocolStack;
import  pl.edu.agh.jgroups.hashmap.*;

import java.net.InetAddress;

public class App {

    private static App instance;
    private static boolean isRunning = true;
    private static String CLUSTER_NAME = "hashmap";

    private static JChannel channel;
    private static DistributedMap map;
    private static Receiver receiver;

    public App() throws Exception {
        this.channel = new JChannel();
        map = new DistributedMap();
        receiver = new Receiver(channel, map);
        initChannelSettings();
        listen();
    }

    private void listen() throws Exception {
        channel.connect(CLUSTER_NAME);
        channel.getState(null, 0);

    }

    private void initChannelSettings() throws Exception {
        /*
        ProtocolStack stack = new ProtocolStack();
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.200.x")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2());
        stack.init();
        stack.setChannel(channel);
        */
        channel.setReceiver(receiver);
    }

    public static void main(String[] args) {
        try {
            instance = new App();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setProperty("java.net.preferIPv4Stack","true");

        Thread transmitThread = new Thread(new Transmitter(channel));
        transmitThread.start();

        while(isRunning) {
            try {
                System.out.println(map.toString());
                Thread.sleep(8000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            transmitThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
