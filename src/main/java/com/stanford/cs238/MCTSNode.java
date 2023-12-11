package com.stanford.cs238;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MCTSNode {

    GomokuEnv env;
    int[][] board;
    MCTSNode parent;
    int action;
    int visits;
    double value;

    Set<MCTSNode> children;
    ArrayList<Integer> availableMoves;

    MCTSNode(GomokuEnv env, int[][] board, MCTSNode parent, int action) {
        this.env = env;
        this.board = board;
        this.parent = parent;
        this.action = action;
        this.visits = 0;
        this.value = 0;

        children = new HashSet<>();
        availableMoves = env.availableMoves(board);
    }

    public double ucb(double c) {
        double qValue = 1 - ((this.value / (double) this.visits) + 1) / 2;
        return qValue + (c * Math.sqrt(Math.log(this.visits) / (double) this.visits));
    }
}
