package pl.edu.agh.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.StateTransferException;
import pl.edu.agh.jgroups.InternalMessage.*;

import java.util.Scanner;

public class Transmitter implements Runnable {

    private JChannel channel;

    public Transmitter(JChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            try {
                String line = scanner.nextLine();
                String[] tokens = line.split(" ");
                switch (tokens[0]) {
                    case "P":
                        channel.send(new Message(null, new PutMessage(tokens[1], Integer.parseInt(tokens[2]))));
                        break;
                    case "R":
                        channel.send(new Message(null, new RemoveMessage(tokens[1])));
                        break;
                    case "D":
                        channel.disconnect();
                        break;
                    case "C":
                        channel.connect(tokens[1]);
                        channel.getState(null, 0);
                    default:
                        break;
                }
            } catch (StateTransferException e) {
                channel.disconnect();
                System.out.println("State transder failed. Try to reconnect again.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
