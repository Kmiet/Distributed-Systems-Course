package pl.edu.agh.akka.bookshop.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class App {

    public static void main(String[] args) throws Exception {

        File configFile = new File("client.conf");
        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("bookshop_client", config);
        final ActorRef actor = system.actorOf(Props.create(BookshopService.class), "bookshop_actor");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            actor.tell(line, null);
        }

        system.terminate();
    }
}

