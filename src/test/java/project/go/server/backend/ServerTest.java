package project.go.server.backend;

import static org.junit.Assert.fail;

import org.junit.Test;

import project.go.Config;
import project.go.server.common.json.Connection;
import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;

public class ServerTest {

    static void log(String msg) {
        Logger.getInstance().log("ServerTest", msg);
    }


    /**
     * Tests a basic connection from a MockClient to the Server.
     * Verifies that:
     * a) The server starts without exceptions.
     * b) The MockClient can connect to the server.
     * c) The MockClient receives some data from the server upon connection.
     */
    @Test
    public void testMockClientConnection() {
        Server server;
        try {
            // Pass true to see verbose server logs
            server = new Server(Logger.LEVEL_ERROR, Config.PORT);
            server.async();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Server failed to start: " + e.getMessage());
            return;
        }

        try {
            MockClient client = new MockClient();
            client.connect();
            String resp = client.receive();
            if (resp == null || resp.isEmpty()) {
                fail("MockClient did not receive any data from server");
            } else {
                log("Received from server: " + resp);
            }
            client.close();
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail("MockClient failed to connect to server");
        }
    }

    /**
     * Tests that two MockClients can connect to the Server and get matched into a game.
     * Verifies that:
     * a) Both clients receive their player side assignments (BLACK/WHITE).
     * b) The sides are opposite for the two clients.
     */
    @Test
    public void testMatchMaking() {
        int port = Config.PORT + 1;
        Server server;
        try {
            // Pass LEVEL_ALL to see verbose server logs
            server = new Server(Logger.LEVEL_ERROR, port);
            server.async();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Server failed to start: " + e.getMessage());
            return;
        }

        // Connect two mock clients and verify they get matched
        try {
            MockClient client1 = new MockClient(port);
            client1.connect();
            String resp1 = client1.receive();
            log("Client1 received: " + resp1);
            Connection conn = JsonFmt.fromJson(resp1, Connection.class);
            if (conn.getClientId().isEmpty()) {
                fail("Client1 did not receive a valid client ID from server");
            }
            
            MockClient client2 = new MockClient(port);
            client2.connect();
            String resp2 = client2.receive();
            log("Client2 received: " + resp2);
            Connection conn2 = JsonFmt.fromJson(resp2, Connection.class);
            if (conn2.getClientId().isEmpty() || conn2.getClientId().equals(conn.getClientId())) {
                fail("Client2 did not receive a valid unique client ID from server");
            }

            String side1 = client1.receive();
            String side2 = client2.receive();
            log("Client1 side: " + side1);
            log("Client2 side: " + side2);

            // Make sure the sides are opposite, and both are valid json format
            if (side1 == null || side2 == null || side1.isEmpty() || side2.isEmpty()) {
                fail("One of the clients did not receive side assignment");
            }

            GameResponse<?> pt1 = JsonFmt.fromJson(side2, GameResponse.class);
            GameResponse<?> pt2 = JsonFmt.fromJson(side1, GameResponse.class);
            
            if (!(pt1.getData() instanceof GameResponse.PlayerTurn) || !(pt2.getData() instanceof GameResponse.PlayerTurn)) {
                fail("One of the clients received invalid side assignment data");
            }

            GameResponse.PlayerTurn playerTurn1 = (GameResponse.PlayerTurn) pt1.getData();
            GameResponse.PlayerTurn playerTurn2 = (GameResponse.PlayerTurn) pt2.getData();

            if (playerTurn1.getColor().equals(playerTurn2.getColor())) {
                fail("Both clients received the same side assignment");
            }

            client1.close();
            client2.close();
            server.stop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail("MockClients failed during match making test");
        }
    }

    /**
     * Tests a mock match play between 2 MockClients connected to the Server.
     * Verifies that:
     * a) Both clients can connect and get matched.
     * b) Each client gets their side assignment.
     * c) Each client can send at any time a move command to the server
     * and get non-blocking responses (not waiting for their turn).
     * d) At least one move from either client is accepted by the server.
     */
    @Test
    public void testMatchPlay() {
        int port = Config.PORT + 2;
        Server server;
        try {
            // Pass LEVEL_ALL to see verbose server logs
            server = new Server(Logger.LEVEL_ERROR, port);
            server.async();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Server failed to start: " + e.getMessage());
            return;
        }

        // Connect two mock clients and verify they get matched
        try {
            MockClient client1 = new MockClient(port);
            client1.connect();
            String cl1Id = client1.receive();
            log("Client1 received: " + cl1Id);
            MockClient client2 = new MockClient(port);
            client2.connect();
            String cl2Id = client2.receive();
            log("Client2 received: " + cl2Id);

            String side1 = client1.receive();
            String side2 = client2.receive();
            log("Client1 side: " + side1);
            log("Client2 side: " + side2);

            // Simulate a few moves
            client1.send(JsonFmt.toJson(new GameCommand<GameCommand.PayloadMakeMove>(
                GameCommand.COMMAND_MAKE_MOVE, cl1Id, new GameCommand.PayloadMakeMove("0101"))));
            
            String resp1 = client1.receive();
            log("Client1 move response: " + resp1);

            // Illegal move
            client1.send(JsonFmt.toJson(new GameCommand<GameCommand.PayloadMakeMove>(
                GameCommand.COMMAND_MAKE_MOVE, cl1Id, new GameCommand.PayloadMakeMove("0101"))));

            String respIllegal = client1.receive();
            log("Client1 illegal move response: " + respIllegal);

            client2.send(JsonFmt.toJson(new GameCommand<GameCommand.PayloadMakeMove>(
                GameCommand.COMMAND_MAKE_MOVE, cl2Id, new GameCommand.PayloadMakeMove("0202"))));
            String movenotify = client2.receive();
            String resp2 = client2.receive();
            log("Client2 move response: " + resp2);

            // Parse responses to ensure at least one move was accepted
            GameResponse<?> notify = JsonFmt.fromJson(movenotify, GameResponse.class);
            GameResponse<?> gr1 = JsonFmt.fromJson(resp1, GameResponse.class);
            GameResponse<?> gr2 = JsonFmt.fromJson(resp2, GameResponse.class);
            GameResponse<?> grIllegal = JsonFmt.fromJson(respIllegal, GameResponse.class);

            if (gr1.isError() && gr2.isError()) {
                fail("Both clients' moves were rejected");
            }

            if (!(gr1.getData() instanceof GameResponse.BoardUpdate) ||
                 !(gr2.getData() instanceof GameResponse.BoardUpdate)) {
                fail("No valid move data received from either client");
            }

            if (!grIllegal.isError()) {
                fail("Illegal move was accepted by the server");
            }

            if (notify.isError() || (!(notify.getData() instanceof GameResponse.BoardUpdate))) {
                fail("Move notification to opponent failed");
            }

            client1.close();
            client2.close();
            server.stop();

        } catch (Exception e) {
            e.printStackTrace();
            fail("MockClients failed during match play test: " + e.getMessage());
        }
    }
}
