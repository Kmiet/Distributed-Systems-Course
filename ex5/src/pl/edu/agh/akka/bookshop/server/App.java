package pl.edu.agh.akka.bookshop.server;

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

        File configFile = new File("server.conf");
        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("bookshop_server", config);
        final ActorRef orders = system.actorOf(Props.create(OrderingService.class, "db/orders.txt"), "ordering_service");
        final ActorRef db = system.actorOf(Props.create(DatabaseService.class), "database_supervisor");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            db.tell(line, null);
        }

        system.terminate();
    }
}
