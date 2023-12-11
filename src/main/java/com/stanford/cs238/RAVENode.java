package com.stanford.cs238;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RAVENode {

    GomokuEnv env;
    int[][] board;
    RAVENode parent;
    double[] mcCount;
    double[] mcValue;
    double[] amafCount;
    double[] amafValue;

    Set<RAVENode> children;
    ArrayList<Integer> availableMoves;

    RAVENode(GomokuEnv env, int[][] board, RAVENode parent) {
        this.env = env;
        this.board = board;
        this.parent = parent;

        mcCount = new double[env.gameSize * env.gameSize];
        mcValue = new double[env.gameSize * env.gameSize];
        amafCount = new double[env.gameSize * env.gameSize];
        amafValue = new double[env.gameSize * env.gameSize];

        children = new HashSet<>();
        availableMoves = env.availableMoves(board);
    }

    RAVENode(GomokuEnv env, int[][] board, RAVENode parent, double[] mcCount, double[] mcValue,
             double[] amafCount, double[] amafValue) {
        this.env = env;
        this.board = board;
        this.parent = parent;

        this.mcCount = mcCount;
        this.mcValue = mcValue;
        this.amafCount = amafCount;
        this.amafValue = amafValue;

        children = new HashSet<>();
        availableMoves = env.availableMoves(board);
    }
}
