package project.go.applogic;

import project.go.dbinterface.DBMove;
/**
 * Class singleMove contains a tuplet of coordinates
 */
public class SingleMove {
    private int x;
    private int y;

    public SingleMove(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setX(int x){
        this.x=x;
    }

    public void setY(int y){
        this.y=y;
    }

    public DBMove convertToDBMove(Color color){
        String colorString;
        if(color==Color.BLACK){
            colorString="black";
        }
        else if(color==Color.WHITE){
            colorString="white";
        }
        else{
            colorString="none";
        }
        return new DBMove(colorString, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SingleMove other = (SingleMove) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
