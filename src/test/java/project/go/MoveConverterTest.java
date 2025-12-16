package project.go;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import project.go.applogic.MoveConverter;
import project.go.applogic.SingleMove;

public class MoveConverterTest {
    @Test
    public void MoveConvertertoJSONTest(){
        SingleMove singleMove = new SingleMove(0,0);
        String output=MoveConverter.toJSON(singleMove);
        assertEquals("0000", output);

        singleMove.setX(10);
        output=MoveConverter.toJSON(singleMove);
        assertEquals("1000",output);

        singleMove.setY(12);
        output=MoveConverter.toJSON(singleMove);
        assertEquals("1012",output);

        assertEquals("", MoveConverter.toJSON(null));
    }

    @Test
    public void MoveConverterfromJSONTest(){

        assertEquals(0, MoveConverter.fromJSON("0000").getY());

        assertEquals(18, MoveConverter.fromJSON("1800").getX());

        assertEquals(-1, MoveConverter.fromJSON("abcd").getX());

    }
}
