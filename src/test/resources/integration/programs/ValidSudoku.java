class ValidSudoku {

    int[][] board;

    ValidSudoku(int[][] board) {
        this.board = board;
    }

    boolean isValid() {
        int[][] rows = new int[9][9];
        int[][] cols = new int[9][9];
        int[][] zones = new int[9][9];

        for (int row=0; row<board.length; row++) {
            for (int col=0; col<board[0].length; col++) {
                int num = board[row][col] - 1;
                if (num == -1) continue;

                if (rows[row][num] != 0 || cols[col][num] != 0) return false;

                int zone = ((row / 3) * 3) + (col / 3);
                if (zones[zone][num] != 0) return false;

                rows[row][num] = 1;
                cols[col][num] = 1;
                zones[zone][num] = 1;
            }
        }

        return true;
    }

    private static void testTrue() {
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

        ValidSudoku validator = new ValidSudoku(board);
        System.out.println(validator.isValid()); // true
    }

    private static void testFalse() {
        int[][] board = new int[][]{
                {8, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        ValidSudoku validator = new ValidSudoku(board);
        System.out.println(validator.isValid()); // false
    }

    public static void main(String[] args) {
        testTrue();
        testFalse();
    }
}