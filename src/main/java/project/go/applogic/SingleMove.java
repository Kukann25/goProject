package project.go.applogic;

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

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof SingleMove)) return false;
        SingleMove move = (SingleMove) o;
        return x==move.x && y==move.y;
    }

    @Override
    public int hashCode(){
        return 31*x + y;
    }
}
