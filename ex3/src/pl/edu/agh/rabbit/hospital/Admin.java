package pl.edu.agh.rabbit.hospital;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.rabbit.hospital.messaging.ResponseReceiver;

public class Admin {

    private static final String EXCHANGE_NAME = "hospital";

    public static void main(String[] main) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Thread receiverThread = new Thread(
                new ResponseReceiver(EXCHANGE_NAME, channel, "#", false)
        );
        receiverThread.start();
        receiverThread.join();
    }
}
