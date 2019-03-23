package pl.edu.agh.jgroups;

import org.jgroups.JChannel;

import  pl.edu.agh.jgroups.hashmap.*;

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
        channel.setReceiver(receiver);
    }

    public static void main(String[] args) {
        try {
            instance = new App();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setProperty("java.net.preferIPv4Stack","true");
        // new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.200.x"))

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
