package project.go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import project.go.dbinterface.MatchRepository;
import project.go.server.backend.Server;

/**
 * Server application main class
 * 
 * mvn compile exec:java -Dexec.mainClass="project.go.App"
 */

@SpringBootApplication
public class App 
{ 
    public static void main( String[] args ) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
            MatchRepository matchRepository = context.getBean(MatchRepository.class);
            Server server = new Server(matchRepository);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}