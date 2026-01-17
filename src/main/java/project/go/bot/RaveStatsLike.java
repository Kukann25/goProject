package project.go.bot;

// Rapid Action Value Estimation (RAVE) selection policy
// Reference: https://en.wikipedia.org/wiki/Monte_Carlo_tree_search#Improvements
// Use this only for game with highly branching factor and transposable states,
// meaning the moves can be played in different order from given position, and the result
// will be the same. For example: Go, Chess, Tic Tac Toe (transposable positions).

public interface RaveStatsLike {
	// Outcomes contating node's move
	float RaveQ();
	int RawRaveQ();
	// Playouts contating node's move
	int RaveN();
	// Add new outcome, that contains node's move
	void AddQRAVE(float q);
	// Increment playouts with
	void AddNRAVE(int n);
}
