package com.stanford.cs238;

import java.util.ArrayList;
import java.util.Arrays;

public class GomokuEnv {
    enum Result {
        WIN,
        FULL,
        NONE
    }

    int currentPlayer;

    int gameSize;
    GomokuEnv(int size, int startPlayer) {
        this.gameSize = size;
        this.currentPlayer = startPlayer;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
    }

    public int[][] generate() {
        return new int[gameSize][gameSize];
    }

    public Result eval(int[][] board, int action) {
        if (action != -1) {
            if (BoardUtils.checkForWin(board, action)) {
                return Result.WIN;
            }
            if (availableMoves(board).size() == 0) return Result.FULL;
        }
        return Result.NONE;
    }

    public int[][] step(int[][] board, int action, int player) {
        int[][] tempBoard = BoardUtils.copy(board);
        int[] index = BoardUtils.unravelIndex(action, gameSize);
        int row = index[0];
        int col = index[1];
        tempBoard[row][col] = player;
        currentPlayer *= -1;
        return tempBoard;
    }

    public ArrayList<Integer> availableMoves(int[][] board) {
        return BoardUtils.ravelIndexWhereZero(board);
    }

    public int[][] invert(int[][] board) {
        return BoardUtils.invert(board);
    }

    public void printBoard(int[][] board) {
        System.out.println(Arrays.deepToString(board)
                .replace("], ", "\n")
                .replace("[[", "")
                .replace("]]", "")
                .replace("[", "")
                .replace(",", "\t"));
    }
}
