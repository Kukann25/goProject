package project.go;

import project.go.server.backend.Server;

/**
 * Server application main class
 * 
 * mvn compile exec:java -Dexec.mainClass="project.go.App"
 */
public class App 
{ 
    public static void main( String[] args ) {
        try {
            Server server = new Server();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}