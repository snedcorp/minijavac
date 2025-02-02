class TestClass {

    public static void main(String[] args) {
        Other o = new Other();
        o.x = 1;
        int x = o.xm(1, true, new int[2][2]);
    }
}

class Other {
    private int x;
    private int xm(int i, boolean b, int[][] ix) {
        return 1;
    }
}