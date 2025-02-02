class Test {
    void main() {
        int[] ix = new int[]{1, {2}, 3, true};
        int[][] ix2 = new int[][]{1, {false, {2}}, 3, 4};
        int[][] ix3 = new int[][][]{1, {{false}}, 3, 4};
        int[][] ix2b = new int[]{1, 2};
    }
}