package project.go.applogic;

import java.util.HashSet;
import java.util.Set;

/**
 * Class ChainResult contains all chains and their liberties 
 */
public class ChainResult {
    Set<SingleMove> chain = new HashSet<>();
    Set<SingleMove> liberties = new HashSet<>();
}
