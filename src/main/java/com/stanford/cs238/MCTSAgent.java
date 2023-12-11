package com.stanford.cs238;

import java.util.stream.DoubleStream;

public class MCTSAgent {

    GomokuEnv env;
    int numSims;
    double c;

    MCTSAgent(GomokuEnv env, int numSims, double c) {
        this.env = env;
        this.numSims = numSims;
        this.c = c;
    }

    public int run(int[][] board) {
        MCTSNode root = new MCTSNode(this.env, board, null, -1);

        for (int i = 0; i < numSims; i++) {
            MCTSNode node = root;
            int value = 0;

            node = select(node, this.c);

            GomokuEnv.Result state = this.env.eval(node.board, node.action);
            if (state == GomokuEnv.Result.WIN) value = -1;

            if (state == GomokuEnv.Result.NONE) {
                int action = BoardUtils.getRandomMove(node.availableMoves);
                MCTSNode newNode = new MCTSNode(this.env,
                        this.env.invert(this.env.step(node.board, action, 1)),
                        node,
                        action);
                node.children.add(newNode);
                node = newNode;
                state = this.env.eval(node.board, node.action);
                if (state == GomokuEnv.Result.WIN) value = -1;

                if (state == GomokuEnv.Result.NONE) {
                    int[][] rolloutState = BoardUtils.copy(node.board);
                    int rolloutPlayer = 1;
                    while (true) {
                        action = BoardUtils.getRandomMove(this.env.availableMoves(rolloutState));
                        rolloutState = this.env.step(rolloutState, action, rolloutPlayer);
                        state = this.env.eval(rolloutState, action);
                        if (state != GomokuEnv.Result.NONE) {
                            value = 1;
                            if (rolloutPlayer == -1) {
                                value *= -1;
                            }
                            break;
                        }
                        rolloutPlayer *= -1;
                    }
                }
            }

            backPropagate(node, value);
        }

        double[] actionProbabilities = new double[this.env.gameSize * this.env.gameSize];
        for (MCTSNode child : root.children) {
            actionProbabilities[child.action] = child.visits;;
        }

        double sum = DoubleStream.of(actionProbabilities).sum();
        for (int action = 0; action < actionProbabilities.length; action++) {
            actionProbabilities[action] /= sum;
        }
        return BoardUtils.argMax(actionProbabilities);
    }

    private void backPropagate(MCTSNode node, double value) {
        while (true) {
            node.value += value;
            node.visits += 1;

            value *= -1;
            if (node.parent == null) break;
            node = node.parent;
        }
    }

    private MCTSNode select(MCTSNode node, double c) {
        while (true) {
            assert node != null;
            if (!(node.availableMoves.size() == 0 && node.children.size() > 0)) break;
            MCTSNode best_child = null;
            double best_ucb = Double.NEGATIVE_INFINITY;
            for (MCTSNode child : node.children) {
                double current_ucb = child.ucb(c);
                if (current_ucb > best_ucb) {
                    best_ucb = current_ucb;
                    best_child = child;
                }
            }
            node = best_child;
        }
        return node;
    }
}
