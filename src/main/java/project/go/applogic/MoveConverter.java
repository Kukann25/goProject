package project.go.applogic;

import project.go.server.backend.Logger;

public abstract class MoveConverter {

    public static String toJSON(SingleMove singleMove){
        StringBuilder stringBuilder = new StringBuilder();

        try{
            if(singleMove.getX()>=10){
                stringBuilder.append(Integer.toString(singleMove.getX()));
            }
            else{
                stringBuilder.append("0");
                stringBuilder.append(Integer.toString(singleMove.getX()));
            }

            if(singleMove.getY()>=10){
                stringBuilder.append(Integer.toString(singleMove.getY()));
            }
            else{
                stringBuilder.append("0");
                stringBuilder.append(Integer.toString(singleMove.getY()));
            }

            return stringBuilder.toString();
        }
        catch(NullPointerException e){
            Logger.getInstance().log("NullPointerException", "In class MoveConverter: toJSON");
        }

        return "";
    }

    public static SingleMove fromJSON(String input){
        int x=0;
        int y=0;

        try{
            if(input.length()==4){
                x+=Integer.parseInt(input.substring(0, 1))*10;
                x+=Integer.parseInt(input.substring(1, 2));
                y+=Integer.parseInt(input.substring(2, 3))*10;
                y+=Integer.parseInt(input.substring(3, 4));

                SingleMove singleMove=new SingleMove(x, y);

                return singleMove;
            }
        }
        catch(NumberFormatException e){
            Logger.getInstance().log("NumberFormatException", "In class MoveConverter: fromJSON");
        }
        
        SingleMove errorSingleMove = new SingleMove(-1, -1);
        return errorSingleMove;
    }
}
