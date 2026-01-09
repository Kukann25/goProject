package project.go.server.client.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.effect.Light.Point;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import project.go.applogic.Board;

public class BoardComponent extends Canvas {
    // padding for the whole board
    private int boardSize;
    private final double padding = 2;
    private double stoneSize;
    private Function<Point2D, Void> callback = null;

    public BoardComponent(Board board) {
        super(500, 500);
        init(board);
    }

    private void init(final Board board) {
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

        this.registerHandlers();
    }

    private Point2D getCoords(MouseEvent event) {
        Point2D coords = new Point2D(-1, -1);

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
        Point2D closest = coords;
        double distance = 100;
        List<Point2D> queue = new ArrayList<>();

        if (c > 0) {
            if (r > 0) {
                queue.add(new Point2D(r, c));
            }
            if (r < boardSize) {
                queue.add(new Point2D(r+1, c));
            }
        }
        if (c < boardSize) {
            if (r > 0) {
                queue.add(new Point2D(r, c+1));
            }
            if (r < boardSize) {
                queue.add(new Point2D(r+1, c+1));
            }
        }

        // Loop throught the candidates and pick the closest one
        for (Point2D point : queue) {
            double d = Math.sqrt(
                Math.pow(point.getX() - coords.getX(), 2) +
                Math.pow(point.getY() - coords.getY(), 2));
            
            if (d < distance) {
                distance = d;
                closest = point;
                System.out.println("New closest: " + closest);
            }
        }

        // Check the distance to closest intersection (r,c) (r, c+1) (r+1, c) (r+1, c+1)
        System.out.println("Clicked at row=" + r + " col=" + c);
        return coords;
    }

    private void registerHandlers() {
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            Point2D coords = this.getCoords(event);
            if (coords.getX() != -1 && coords.getY() != -1 && callback != null) {
                this.callback.apply(coords);
            }
        });
    }

    public void update(final Board board) {

    }

    public void setOnClick(Function<Point2D, Void> callback) {
        this.callback = callback;
    }
}
