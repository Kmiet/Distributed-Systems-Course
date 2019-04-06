package pl.edu.agh.rabbit.hospital.messaging;

import com.rabbitmq.client.*;
import java.io.IOException;

public class ResponseReceiver implements Runnable {

    private final String EXCHANGE_NAME;
    private final Channel channel;
    private final String topic;
    private final String queueName;

    public ResponseReceiver(String exchangeName, Channel channel, String topic, boolean isDoc) throws Exception {
        this.EXCHANGE_NAME = exchangeName;
        this.channel = channel;
        if(isDoc) {
            this.topic = ".doctors." + topic;
        } else {
            this.topic = "." + topic;
        }

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        this.queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, EXCHANGE_NAME + this.topic.toLowerCase());
    }

    @Override
    public void run() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };

        try {
            channel.basicConsume(queueName, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
