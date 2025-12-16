package project.go;


import project.go.server.backend.Server;

/**
 * Hello world!
 *
 */
public class App 
{ 
    public static void main( String[] args )
    {
        if (args.length > 0 && args[0].equals("server")) {
            try {
                Server server = new Server();
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equals("client")) {
        } else {
            System.out.println("Please specify 'server' or 'client' as an argument.");
        }
    }
}