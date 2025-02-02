class Test {
    int x;
    boolean y;

    Test(int x, boolean y) {
        this.x = x;
        this.y = y;
    }

    Test(int x) {
        this(x, false);
    }

    Test() {
        this(13);
    }
}