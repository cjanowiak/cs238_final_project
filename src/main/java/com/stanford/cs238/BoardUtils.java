package com.stanford.cs238;

import java.util.*;

public class BoardUtils {

    public static int[][] copy(int[][] board){
        int[][] newBoard = new int[board.length][];
        for(int i = 0; i < board.length; i++) {
            int[] row = board[i];
            newBoard[i] = new int[board.length];
            System.arraycopy(row, 0, newBoard[i], 0, board.length);
        }
        return newBoard;
    }

    public static ArrayList<Integer> ravelIndexWhereZero(int[][] board) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                if (board[row][col] == 0) {
                    indexes.add(row * board.length + col);
                }
            }
        }
        return indexes;
    }

    public static int[] unravelIndex(int index, int size) {
        int[] unraveledIndex = new int[2];
        unraveledIndex[0] = index / size;
        unraveledIndex[1] = index % size;
        return unraveledIndex;
    }

    public static int[][] invert(int[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                board[row][col] *= -1;
            }
        }
        return board;
    }

    public static int countUnbrokenLines(int[][] board, int target, int length) {
        int numRows = board.length;
        int numCols = board[0].length;
        int count = 0;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j <= numCols - length; j++) {
                if (checkLine(board, i, j, 0, 1, target, length)) {
                    count++;
                }
            }
        }

        for (int i = 0; i <= numRows - length; i++) {
            for (int j = 0; j < numCols; j++) {
                if (checkLine(board, i, j, 1, 0, target, length)) {
                    count++;
                }
            }
        }

        for (int i = 0; i <= numRows - length; i++) {
            for (int j = 0; j <= numCols - length; j++) {
                if (checkLine(board, i, j, 1, 1, target, length)) {
                    count++;
                }
            }
        }

        for (int i = 0; i <= numRows - length; i++) {
            for (int j = length - 1; j < numCols; j++) {
                if (checkLine(board, i, j, 1, -1, target, length)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean checkLine(int[][] board, int startRow, int startCol, int dirRow, int dirCol, int target, int length) {
        for (int i = 0; i < length; i++) {
            if (board[startRow + i * dirRow][startCol + i * dirCol] != target) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkDirection(int[][] board, int player, int row, int col, int rowIncrement, int colIncrement) {
        for (int i = -4; i <= 4; i++) {
            int r = row + i * rowIncrement;
            int c = col + i * colIncrement;

            if (isValidPosition(r, c) && board[r][c] == player) {
                int consecutiveCount = 1;
                for (int j = 1; j <= 4; j++) {
                    int nextRow = row + (i + j) * rowIncrement;
                    int nextCol = col + (i + j) * colIncrement;

                    if (isValidPosition(nextRow, nextCol) && board[nextRow][nextCol] == player) {
                        consecutiveCount++;
                    } else {
                        break;
                    }
                }

                if (consecutiveCount == 5) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkForWin(int[][] board, int action) {
        int[] index = BoardUtils.unravelIndex(action, 15);
        int row = index[0];
        int col = index[1];
        int player = board[row][col];
        return checkDirection(board, player, row, col, 0, 1) ||
                checkDirection(board, player, row, col, 1, 0) ||
                checkDirection(board, player, row, col, 1, 1) ||
                checkDirection(board, player, row, col, 1, -1);
    }

    private static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
    }

    public static double boardEval(int[][] board) {
        double value = 0;
        for (int i = 5; i > 2; i--) {
            value += countUnbrokenLines(board, 1, i) * i;
            value -= countUnbrokenLines(board, -1, i) * i;
        }
        return value;
    }

    public static int argMax(double[] a) {
        double v = Double.MIN_VALUE;
        int ind = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > v) {
                v = a[i];
                ind = i;
            }
        }
        return ind;
    }

    public static int argMin(double[] a) {
        double v = Double.MAX_VALUE;
        int ind = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < v) {
                v = a[i];
                ind = i;
            }
        }
        return ind;
    }

    public static boolean equals(int[][] board1, int[][] board2) {
        if (board1.length != board2.length) {
            return false;
        }
        for (int i = 0; i < board1.length; i++) {
            if (!Arrays.equals(board1[i], board2[i])) {
                return false;
            }
        }
        return true;
    }

    public static int getRandomMove(List<Integer> moves) {
        return moves.remove(new Random().nextInt(moves.size()));
    }
}
