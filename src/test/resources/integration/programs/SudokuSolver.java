class SudokuSolver {

    int[][] board;
    int[][] rows;
    int[][] cols;
    int[][] zones;
    boolean found;

    SudokuSolver(int[][] board) {
        this.board = board;
        rows = new int[9][9];
        cols = new int[9][9];
        zones = new int[9][9];
        setupBoard();
    }

    private void setupBoard() {
        for (int row=0; row<board.length; row++) {
            for (int col=0; col<board[0].length; col++) {
                int num = board[row][col] - 1;
                if (num == -1) continue;

                int zone = ((row / 3) * 3) + (col / 3);
                addNumber(row, col, zone, num);
            }
        }
    }

    public void solve() {
        backtrack(0, 0);
    }

    private void backtrack(int row, int col) {
        if (row == 9) {
            found = true;
            return;
        }

        int num = board[row][col] - 1;

        int nextRow = -1;
        int nextCol = -1;

        if (col == 8) {
            nextRow = row+1;
            nextCol = 0;
        } else {
            nextRow = row;
            nextCol = col+1;
        }

        if (num != -1) {
            backtrack(nextRow, nextCol);
            return;
        }

        int zone = ((row / 3) * 3) + (col / 3);

        for (int i=0; i<9; i++) {
            if (rows[row][i] != 0 || cols[col][i] != 0 ||
                    zones[zone][i] != 0) continue;

            addNumber(row, col, zone, i);
            backtrack(nextRow, nextCol);

            if (found) {
                board[row][col] = i+1;
                return;
            }
            removeNumber(row, col, zone, i);
        }
    }

    private void addNumber(int row, int col, int zone, int num) {
        rows[row][num] += 1;
        cols[col][num] += 1;
        zones[zone][num] += 1;
    }

    private void removeNumber(int row, int col, int zone, int num) {
        rows[row][num] -= 1;
        cols[col][num] -= 1;
        zones[zone][num] -= 1;
    }

    public static void main(String[] args) {
        int[][] board = new int[][]{
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        SudokuSolver solver = new SudokuSolver(board);
        solver.solve();

        for (int i=0; i<9; i++) {
            for (int j=0; j<9; j++) {
                System.out.println(board[i][j]);
            }
        }
    }
}