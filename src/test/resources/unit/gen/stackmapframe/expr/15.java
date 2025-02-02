class Test {
    static void main(boolean b) {
        int[][] i2 = new int[1][2];
        Test[][] t2 = new Test[1][2];
        int r = get(i2[0][1], t2[0][1], !b);
    }

    static int get(int i, Test t, boolean b) {
        return 1;
    }
}