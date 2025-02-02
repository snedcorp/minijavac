class Test {
    static void main(boolean b) {
        float f = 1.0;
        Test t = new Test();
        Test[] tarr = new Test[2];
        int[][] iarr = new int[2][2];
        int r = get(f, t, tarr, iarr, !b);
    }

    static int get(float f, Test t, Test[] tarr, int[][] iarr, boolean b) {
        return 1;
    }
}