package project.go;

import project.go.server.backend.Server;
import project.go.server.client.Client;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        if (args.length != 1) {
            System.out.println("Args required: [server|client]");
            return;
        }

        if (args[0].equals("server")) {
           try {
                Server server = new Server();
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("client")) {
            Client client = new Client();
            client.run();
        } else {
            System.out.println("Unknown argument: " + args[0]);
            System.out.println("Args required: [server|client]");
        }
    }
}
