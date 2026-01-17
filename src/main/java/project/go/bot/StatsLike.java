package project.go.bot;

import javafx.util.Pair;

public interface StatsLike<T> {
    int N();
	int VirtualLoss();
	void AddQ(float q);
	float AvgQ();
	float Q();
	void SetVvl(int visits, int vl);
	Pair<Integer, Integer> GetVvl(); 
	void AddVvl(int visits, int vl);
	int RealVisits();
	T Clone();
}
