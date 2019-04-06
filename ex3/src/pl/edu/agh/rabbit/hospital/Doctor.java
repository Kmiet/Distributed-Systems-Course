package pl.edu.agh.rabbit.hospital;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.rabbit.hospital.messaging.ResponseReceiver;
import pl.edu.agh.rabbit.hospital.messaging.TaskSender;

import java.util.Scanner;

public class Doctor {

    private static final String EXCHANGE_NAME = "hospital";

    public static void main(String[] args) throws Exception {
        String name = new String();

        Scanner scanner = new Scanner(System.in);
        while(name.isEmpty()) {
            System.out.println("Enter: DOCTOR_NAME");
            name = scanner.nextLine();
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();

        Thread senderThread = new Thread(
                new TaskSender(EXCHANGE_NAME, channel1, name)
        );
        Thread receiverThread = new Thread(
                new ResponseReceiver(EXCHANGE_NAME, channel2, name, true)
        );

        senderThread.start();
        receiverThread.start();

        senderThread.join();
        receiverThread.join();
    }
}
