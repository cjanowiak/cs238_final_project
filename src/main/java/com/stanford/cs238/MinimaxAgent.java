package com.stanford.cs238;

import java.util.ArrayList;
import java.util.Arrays;

public class MinimaxAgent {

    GomokuEnv env;
    int depth;
    int agentPlayer;


    MinimaxAgent(GomokuEnv env, int depth, int agentPlayer) {
        this.env = env;
        this.depth = depth;
        this.agentPlayer = agentPlayer;
    }

    public int run(int[][] board) {
        ArrayList<Integer> moves = this.env.availableMoves(board);
        double[] actionValues = new double[this.env.gameSize * this.env.gameSize];
        Arrays.fill(actionValues, Double.NEGATIVE_INFINITY);
        boolean maximizing = this.agentPlayer != -1;

        for (int action : moves) {
            MCTSNode root = new MCTSNode(this.env, env.step(board, action, this.agentPlayer), null, action);
            actionValues[action] = minimax(root,
                    Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    this.depth,
                    maximizing,
                    this.agentPlayer);
        }

        int idx = BoardUtils.argMax(actionValues);
        if (idx == -1) idx = BoardUtils.getRandomMove(moves);
        return idx;
    }

    private double minimax(MCTSNode root, double alpha, double beta, int depth, boolean maximizing,
                           int player) {
        if (depth == 0 || root.availableMoves.isEmpty()) {
            return BoardUtils.boardEval(root.board);
        }

        if (maximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (int action : root.availableMoves) {
                double result = minimax(new MCTSNode(this.env, env.step(root.board, action, player),
                        root, action), alpha, beta, depth - 1, false, player *= -1);
                maxEval = Double.max(maxEval, result);
                alpha = Double.max(alpha, result);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (int action : root.availableMoves) {
                double result = minimax(new MCTSNode(this.env, env.step(root.board, action, player),
                        root, action), alpha, beta, depth - 1, true, player *= -1);
                minEval = Double.min(minEval, result);
                beta = Double.min(beta, result);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

}
