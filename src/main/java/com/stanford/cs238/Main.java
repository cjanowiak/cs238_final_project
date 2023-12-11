package com.stanford.cs238;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
//        GomokuEnv game = new GomokuEnv(15, 1);
//        MCTSAgent mctsAgent = new MCTSAgent(game, 10000, 1.4);
//        MCRAVEAgent mcraveAgent = new MCRAVEAgent(game, 5000, -1);
//        MinimaxAgent minimaxAgent = new MinimaxAgent(game, 2, 1);

        int numThreads = Runtime.getRuntime().availableProcessors();
        String MCTSOutputFileName = "src/main/resources/MCTS_sims_results.txt";
        String MCRAVEOutputFileName = "src/main/resources/MCRAVE_sims_results.txt";
        String MCRAVEHeuristicOutputFileName = "src/main/resources/MCRAVE_heuristic_results.txt";
        Map<String, Double> threadOutputsMCTS = new HashMap<>();
        Map<String, Double> threadOutputsMCRAVE = new HashMap<>();
        Map<String, Double> threadOutputsMCRAVEHeuristic = new HashMap<>();

        int simulateGames = 100;

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        double b = Math.pow(10, -13);
        double c = 0.5;

        for (int numSims = 200; numSims <= 5000; numSims += 200) {

            int sims = numSims;
            Runnable task1 = () -> {
                GomokuEnv game1 = new GomokuEnv(15, 1);
                MinimaxAgent minimaxAgent1 = new MinimaxAgent(game1, 2, 1);
                MCTSAgent mctsAgent = new MCTSAgent(game1, sims, c);
                double result1 = simulateMCTS(game1, mctsAgent, minimaxAgent1, simulateGames);
                threadOutputsMCTS.put("numSims = " + sims, result1);
                System.out.println("Finished simulating MCTS numSims = " + sims);
            };

            threadPool.submit(task1);

            Runnable task2 = () -> {
                GomokuEnv game2 = new GomokuEnv(15, 1);
                MinimaxAgent minimaxAgent2 = new MinimaxAgent(game2, 2, 1);
                MCRAVEAgent mcraveAgent1 = new MCRAVEAgent(game2, sims, -1, b, false);
                double result2 = simulateMCRave(game2, mcraveAgent1, minimaxAgent2, simulateGames);
                threadOutputsMCRAVE.put("numSims = " + sims, result2);
                System.out.println("Finished simulating MCRAVE numSims = " + sims);
            };

            threadPool.submit(task2);

            Runnable task3 = () -> {
                GomokuEnv game3 = new GomokuEnv(15, 1);
                MinimaxAgent minimaxAgent3 = new MinimaxAgent(game3, 2, 1);
                MCRAVEAgent mcraveAgent2 = new MCRAVEAgent(game3, sims, -1, b, true);
                double result3 = simulateMCRave(game3, mcraveAgent2, minimaxAgent3, simulateGames);
                threadOutputsMCRAVEHeuristic.put("numSims = " + sims, result3);
                System.out.println("Finished simulating MCRAVE Heuristic numSims = " + sims);
            };

            threadPool.submit(task3);
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writeMapToFile(MCTSOutputFileName, threadOutputsMCTS);
        writeMapToFile(MCRAVEOutputFileName, threadOutputsMCRAVE);
        writeMapToFile(MCRAVEHeuristicOutputFileName, threadOutputsMCRAVEHeuristic);
    }

    private static void writeMapToFile(String fileName, Map<String, Double> map) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                writer.println(entry.getKey() + " - win percentage: " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double simulateMCTS(GomokuEnv game, MCTSAgent mctsAgent,
                                 MinimaxAgent minimaxAgent,
                                 int numSims) {
        int player = 1;
        int wins = 0;

        for (int i = 0; i < numSims; i++) {
            int[][] state = game.generate();

            while (true) {
                int action;
                if (player == 1) {
                    action = minimaxAgent.run(state);
                } else {
                    action = mctsAgent.run(BoardUtils.invert(state));
                    state = BoardUtils.invert(state);
                }


                state = game.step(state, action, player);
                GomokuEnv.Result result = game.eval(state, action);

                if (result == GomokuEnv.Result.WIN) {
//                    System.out.println("Player " + player + " won");
                    if (player == -1) wins += 1;
                    break;
                } else if (result == GomokuEnv.Result.FULL) {
//                    System.out.println("Draw");
                    break;
                }

                player *= -1;
            }
        }

        return (double) wins / (double) numSims;
    }

    private static double simulateMCRave(GomokuEnv game, MCRAVEAgent mcraveAgent,
                                       MinimaxAgent minimaxAgent,
                                       int numSims) {
        int player = 1;
        int wins = 0;

        for (int i = 0; i < numSims; i++) {
            int[][] state = game.generate();

            while (true) {
                int action;
                if (player == 1) {
                    action = minimaxAgent.run(state);
                } else {
                    action = mcraveAgent.run(state);
                }


                state = game.step(state, action, player);
                GomokuEnv.Result result = game.eval(state, action);

                if (result == GomokuEnv.Result.WIN) {
//                    System.out.println("Player " + player + " won");
                    if (player == -1) wins += 1;
                    break;
                } else if (result == GomokuEnv.Result.FULL) {
//                    System.out.println("Draw");
                    break;
                }

                player *= -1;
            }
        }

        return (double) wins / (double) numSims;
    }

}