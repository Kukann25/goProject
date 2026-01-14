package project.go.applogic;

import java.util.HashSet;
import java.util.Set;

public class ChainResult {
    Set<SingleMove> chain = new HashSet<>();
    Set<SingleMove> liberties = new HashSet<>();
}
