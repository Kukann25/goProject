package project.go.dbinterface;

public class DBMove {
    private String color;
    private int x;
    private int y;

    public DBMove() {}

    public DBMove(String color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public void setX(int x){
        this.x=x;
    }

    public int getY(){
        return y;
    }

    public void setY(int y){
        this.y=y;
    }

    public String getColor(){
        return color;
    }

    public void setColor(String color){
        this.color=color;
    }

}