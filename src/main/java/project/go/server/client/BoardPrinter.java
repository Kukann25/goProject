package project.go.server.client;

import project.go.applogic.Board;
import project.go.applogic.Color;

public class BoardPrinter {
    
    public static void printBoard(Board board) {
        int size = board.getSize();

        // Print coordinate header
        SyncPrinter.print("   ");
        for (int x = 0; x < size; x++) {
            System.out.printf("%02d ", x);
        }

        SyncPrinter.println("");
        Color[][] cells = board.returnCurrentState();
        for (int y = 0; y < size; y++) {
            System.out.printf("%02d ", y);
            for (int x = 0; x < size; x++) {
                switch (cells[x][y]) {
                    case WHITE:
                        SyncPrinter.print(" \033[0;37m●\033[0m ");
                        break;
                    case BLACK:
                        SyncPrinter.print(" \033[0;37m○\033[0m ");
                        break;
                    case NONE:
                        SyncPrinter.print(" . ");
                        break;
                }
            }
            SyncPrinter.println("");
        }
    }
}
