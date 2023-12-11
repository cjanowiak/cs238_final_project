package com.stanford.cs238;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

/*
 *  Code is adopted from Gelly and Silver’s heuristic MC-RAVE algorithm.
 *  https://doi.org/10.1016/j.artint.2011.03.007
 */

public class MCRAVEAgent {

    GomokuEnv env;
    int numSims;
    int player;
    double b;
    boolean heuristic;

    MCRAVEAgent(GomokuEnv env, int numSims, int player, double b, boolean heuristic) {
        this.env = env;
        this.numSims = numSims;
        this.player = player;
        this.b = b;
        this.heuristic = heuristic;
    }

    public int run(int[][] board) {
        RAVENode root = new RAVENode(this.env, board, null);
        for (int i = 0; i < numSims; i++) {
            int[][] tempBoard = BoardUtils.copy(board);
            this.env.setCurrentPlayer(this.player);
            simulate(tempBoard, root);
        }
        this.env.setCurrentPlayer(this.player);
        return selectMove(board, root);
    }

    private void simulate(int[][] board, RAVENode node) {
        LinkedHashMap<RAVENode, Integer> treeSim = simTree(board, node);
        ArrayList<RAVENode> states = new ArrayList<>();
        ArrayList<Integer> actions = new ArrayList<>();
        Set<RAVENode> keys = treeSim.keySet();
        for (RAVENode state : keys) {
            states.add(state);
            actions.add(treeSim.get(state));
        }
        env.setCurrentPlayer(env.getCurrentPlayer() * -1);
        int[][] newBoard = this.env.step(states.get(states.size() - 1).board,
                actions.get(actions.size() - 1), env.getCurrentPlayer());

        ArrayList<Integer> simActionsAndResult = simDefault(newBoard);

        for (int i = 0; i < simActionsAndResult.size() - 1; i++) {
            actions.add(simActionsAndResult.get(i));
        }
        backup(states, actions, simActionsAndResult.get(simActionsAndResult.size() - 1));
    }

    private ArrayList<Integer> simDefault(int[][] board) {
        int[][] rolloutState = BoardUtils.copy(board);
        int rolloutPlayer = this.env.currentPlayer;

        ArrayList<Integer> actions = new ArrayList<>();
        while (true) {
            int action = BoardUtils.getRandomMove(this.env.availableMoves(rolloutState));
            actions.add(action);
            rolloutState = this.env.step(rolloutState, action, rolloutPlayer);
            GomokuEnv.Result state = this.env.eval(rolloutState, action);
            if (state != GomokuEnv.Result.NONE) {
                int value = 0;
                if (rolloutPlayer == this.player && state == GomokuEnv.Result.WIN) {
                    value = 1;
                }
                actions.add(value);
                break;
            }
            rolloutPlayer *= -1;
        }
        return actions;
    }

    private LinkedHashMap<RAVENode, Integer> simTree(int[][] board, RAVENode node) {
        if (this.env.availableMoves(board).size() == 0) return new LinkedHashMap<>();
        RAVENode currentNode = node;
        int[][] rolloutBoard = BoardUtils.copy(board);
        LinkedHashMap<RAVENode, Integer> rollout = new LinkedHashMap<>();
        while (true) {
            int action = selectMove(rolloutBoard, currentNode);
            rollout.put(currentNode, action);
            rolloutBoard = this.env.step(rolloutBoard, action, env.getCurrentPlayer());
            if (env.eval(rolloutBoard, action) != GomokuEnv.Result.NONE) break;

            boolean newChild = true;
            for (RAVENode child : currentNode.children) {
                if (BoardUtils.equals(child.board, rolloutBoard)) {
                    currentNode = child;
                    newChild = false;
                }
            }
            if (newChild) {
                RAVENode newNode = newNode(rolloutBoard, currentNode, this.heuristic);
                currentNode.children.add(newNode);
                action = BoardUtils.getRandomMove(this.env.availableMoves(rolloutBoard));
                rollout.put(newNode, action);
                rolloutBoard = this.env.step(rolloutBoard, action, env.getCurrentPlayer());
                return rollout;
            }
        }
        return rollout;
    }

    private RAVENode newNode(int[][] board, RAVENode parent, boolean heuristic) {
        if (heuristic) {
            double[] mcCount = new double[env.gameSize * env.gameSize];
            double[] mcValue = new double[env.gameSize * env.gameSize];
            double[] amafCount = new double[env.gameSize * env.gameSize];
            double[] amafValue = new double[env.gameSize * env.gameSize];
            Arrays.fill(mcCount, 50);
            Arrays.fill(mcValue, 0.5);
            Arrays.fill(amafCount, 50);
            Arrays.fill(amafValue, 0.5);
            return new RAVENode(
                    this.env, board, parent, mcCount, mcValue, amafCount, amafValue
            );
        } else {
            return new RAVENode(
                    this.env, board, parent
            );
        }
    }

    private void backup(ArrayList<RAVENode> states, ArrayList<Integer> actions, int z) {
        for (int t = 0; t < states.size(); t++) {
            RAVENode state = states.get(t);
            int action = actions.get(t);
            state.mcCount[action] += 1;
            state.mcValue[action] += (z - state.mcValue[action]) / state.mcCount[action];
            ArrayList<Integer> previous = new ArrayList<>();
            for (int u = t; u < actions.size(); u += 2) {
                int subAction = actions.get(u);
                if (!previous.contains(subAction)) {
                    state.amafCount[subAction] += 1;
                    state.amafValue[subAction] += (z - state.amafValue[subAction]) / state.amafCount[subAction];
                    previous.add(subAction);
                }
            }
        }
    }

    private int selectMove(int[][] board, RAVENode node) {
        ArrayList<Integer> legalMoves = node.availableMoves;
        double[] evaluations = new double[legalMoves.size()];
        for (int i = 0; i < legalMoves.size(); i ++) {
            evaluations[i] = eval(node, legalMoves.get(i));
        }
        int idx;
        if (this.env.getCurrentPlayer() == this.player) {
            idx = BoardUtils.argMax(evaluations);
        } else {
            idx = BoardUtils.argMin(evaluations);
        }
        if (idx == -1) return BoardUtils.getRandomMove(legalMoves);
        return legalMoves.get(idx);
    }

    private double eval(RAVENode node, int action) {
        double beta =
                (node.amafCount[action]) / (node.mcCount[action] + node.amafCount[action] +
                        (4.0 * node.mcCount[action] * node.amafCount[action] * Math.pow(this.b,
                                2)));
        return ((1.0 - beta) * node.mcValue[action]) + (beta * node.amafValue[action]);
    }
}
