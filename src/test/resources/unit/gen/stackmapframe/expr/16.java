class Test {

    static int i;
    boolean b;
    float f;
    Test t;
    int[][] i2;
    Test[][][] t3;

    void main(boolean b) {
        int r = get(i, f, t, i2, t3, !b);
    }

    static int get(int i, float f, Test t, int[][] i2, Test[][][] t3, boolean b) {
        return 1;
    }
}