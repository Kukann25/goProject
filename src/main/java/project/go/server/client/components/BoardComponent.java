package project.go.server.client.components;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import project.go.applogic.Board;
import project.go.applogic.SingleMove;

public class BoardComponent extends Canvas {

    @FunctionalInterface
    public static interface Callback {
        public void apply(SingleMove coordinates);
    }

    // padding for the whole board
    private int boardSize;
    private final double padding = 2;
    private double stoneSize;
    private Callback callback = null;

    public BoardComponent(Board board) {
        super(500, 500);
        this.draw(board);
        this.registerHandlers();
    }

    private void draw(final Board board) {
        // Size = min (width, height)
        final double sizePx = 
            this.getWidth() > this.getHeight() ? this.getHeight() : this.getWidth();
        final double lineLenght = sizePx - padding*2;
        final double squareSize = lineLenght / (board.getSize());
        double x = padding + squareSize/2, 
               y = padding + squareSize/2;
        this.stoneSize = squareSize;
        this.boardSize = board.getSize();

        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.web("#d1a773"));
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());

        gc.setStroke(Color.BLACK);

        // draw each line - columns then rows
        for (int c = 0; c < board.getSize(); c++, x += squareSize) {
            gc.strokeLine(x, y, x, y + lineLenght - squareSize);
        }

        x = padding + squareSize/2;
        for (int r = 0; r < board.getSize(); r++, y += squareSize) {
            gc.strokeLine(x, y, x + lineLenght - squareSize, y);
        }

        // draw stones
        project.go.applogic.Color[][] cells = board.returnCurrentState();

        double p = padding + stoneSize/2;
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                switch (cells[c][r]) {
                    case BLACK:
                        gc.setFill(Color.BLACK);
                        gc.fillOval(
                            p + c * squareSize - squareSize/4,
                            p + r * squareSize - squareSize/4,
                            squareSize/2,
                            squareSize/2);
                        break;
                    case WHITE:
                        gc.setFill(Color.WHITE);
                        gc.fillOval(
                            p + c * squareSize - squareSize/4,
                            p + r * squareSize - squareSize/4,
                            squareSize/2,
                            squareSize/2);
                        break;
                    case NONE:
                        // do nothing
                        break;
                }
            }
        }
    }

    private SingleMove getCoords(MouseEvent event) {
        SingleMove coords = new SingleMove(-1, -1);

        double p = padding + stoneSize/2;

        if ((event.getX() < padding || event.getX() > this.getWidth() - padding) ||
            (event.getY() < padding || event.getY() > this.getHeight() - padding)) {
            System.out.println("out of boundary: x=" + event.getX() + " y=" + event.getY());
            return coords;
        }

        // In bound, simply divide the coordinates by the square sizes
        int r = (int)Math.floor((event.getY() - p) / stoneSize),
            c = (int)Math.floor((event.getX() - p) / stoneSize);

        // Check the closest intersection
        coords = new SingleMove(c, r);
        double distance = 100;
        List<SingleMove> queue = new ArrayList<>();
        // GraphicsContext gc = this.getGraphicsContext2D();
        // gc.setFill(Color.GREEN);

        if (c >= 0) {
            if (r >= 0) {
                queue.add(new SingleMove(c, r));
                // gc.fillOval(c*stoneSize + p, r*stoneSize + p, stoneSize/2, stoneSize/2);
            }
            if (r < boardSize) {
                queue.add(new SingleMove(c, r+1));
                // gc.fillOval(c*stoneSize + p, (r+1)*stoneSize + p, stoneSize/2, stoneSize/2);
            }
        }
        if (c < boardSize) {
            if (r >= 0) {
                queue.add(new SingleMove(c+1, r));
                // gc.fillOval((c+1)*stoneSize + p, r*stoneSize + p, stoneSize/2, stoneSize/2);
            }
            if (r < boardSize) {
                queue.add(new SingleMove(r+1, c+1));
                // gc.fillOval((c+1)*stoneSize + p, (r+1)*stoneSize + p, stoneSize/2, stoneSize/2);
            }
        }

        
        // Loop throught the candidates and pick the closest one
        for (SingleMove point : queue) {
            double d = Math.sqrt(
                Math.pow(point.getX()*stoneSize + p - event.getX(), 2) +
                Math.pow(point.getY()*stoneSize + p - event.getY(), 2));
            
            // System.out.println("Point: " + point + " dist=" + d);
            if (d < distance) {
                distance = d;
                coords = point;
                // System.out.println("New closest: " + coords + " (d=" + d);
            }
        }

        // gc.setFill(Color.RED);
        // gc.fillOval(coords.getX()*stoneSize + p, coords.getY()*stoneSize + p, stoneSize/2, stoneSize/2);

        return coords;
    }

    private void registerHandlers() {
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            SingleMove coords = this.getCoords(event);
            if (coords.getX() != -1 && coords.getY() != -1 && callback != null) {
                this.callback.apply(new SingleMove(coords.getX(), coords.getY()));
            }
        });
    }

    public void update(final Board board) {
        this.draw(board);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
