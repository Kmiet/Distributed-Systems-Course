package pl.edu.agh.rabbit.hospital.messaging;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TaskReceiver {

    private final String EXCHANGE_NAME;
    private final Channel channel;
    private final String receiverName;
    private final List<String> topics;
    private final List<String> queueNames;

    public TaskReceiver(String exchangeName, Channel channel, String[] topics, String receiverName) throws Exception {
        this.EXCHANGE_NAME = exchangeName;
        this.channel = channel;
        this.topics = new LinkedList<>();
        this.receiverName = receiverName;
        this.queueNames = new LinkedList<>();

        for(String topic : topics) this.topics.add(EXCHANGE_NAME + ".tasks." + topic.toLowerCase());

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        for(String topic : this.topics) {
            String queueName = channel
                    .queueDeclare(topic, false, false, false, null)
                    .getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, topic);
            this.queueNames.add(queueName);
        }
    }

    public void receive() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String[] tokens = message.split(" ");
                System.out.println("Received: " + tokens[1] + " for: " + tokens[2] + " from: " + tokens[0]);
                message = "Task " + tokens[1] + " for patient " + tokens[2] + " is done. - " + receiverName;
                // Doing the task
                try {
                    Thread.sleep(10000);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                channel.basicPublish(EXCHANGE_NAME, "hospital.doctors." + tokens[0].toLowerCase(), null, message.getBytes("UTF-8"));
            }
        };

        try {
            for(String name : this.queueNames) {
                channel.basicConsume(name, true, consumer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
