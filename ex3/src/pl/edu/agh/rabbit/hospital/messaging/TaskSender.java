package pl.edu.agh.rabbit.hospital.messaging;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.util.Scanner;

public class TaskSender implements Runnable {

    private final String EXCHANGE_NAME;
    private final Channel channel;
    private final String senderName;

    public TaskSender(String exchangeName, Channel channel, String senderName) {
        this.EXCHANGE_NAME = exchangeName;
        this.channel = channel;
        this.senderName = senderName;
        initChannel();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            // Task format: TASK_TYPE PATIENT_NAME
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            if(tokens.length == 2) enqueTask(tokens[0], tokens[1]);
        }
    }

    private void initChannel() {
        try {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enqueTask(String type, String patient) {
        String message = this.senderName + " " + type.toUpperCase() + " " + patient;
        try {
            switch (TaskType.parseType(type)) {
                case ELBOW:
                    channel.basicPublish(EXCHANGE_NAME, "hospital.tasks.elbow", null, message.getBytes("UTF-8"));
                    break;
                case HIP:
                    channel.basicPublish(EXCHANGE_NAME, "hospital.tasks.hip", null, message.getBytes("UTF-8"));
                    break;
                case KNEE:
                    channel.basicPublish(EXCHANGE_NAME, "hospital.tasks.knee", null, message.getBytes("UTF-8"));
                    break;
                case UNKNOWN:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
