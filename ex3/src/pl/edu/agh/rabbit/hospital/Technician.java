package pl.edu.agh.rabbit.hospital;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.edu.agh.rabbit.hospital.messaging.TaskReceiver;
import pl.edu.agh.rabbit.hospital.messaging.TaskType;

import java.util.Scanner;

public class Technician {

    private static final String EXCHANGE_NAME = "hospital";

    public static void main(String[] main) throws Exception {
        String name = new String();
        String firstSpec = new String();
        String secondSpec = new String();

        Scanner scanner = new Scanner(System.in);
        while(name.isEmpty() && firstSpec.isEmpty() && secondSpec.isEmpty()) {
            System.out.println("Enter: TECHNICAN_NAME FIRST_SPEC SECOND_SPEC");
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            if(tokens.length == 3 && TaskType.parseType(tokens[1]) != TaskType.UNKNOWN && TaskType.parseType(tokens[2]) != TaskType.UNKNOWN) {
                name = tokens[0];
                firstSpec = tokens[1];
                secondSpec = tokens[2];
            }
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String[] specs = {firstSpec, secondSpec};

        TaskReceiver receiver = new TaskReceiver(EXCHANGE_NAME, channel, specs, name);

        receiver.receive();

    }
}
