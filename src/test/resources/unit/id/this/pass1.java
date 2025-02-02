class Test {

    Test() {}

    Test(int x) {}

    Test(boolean b) {}

    Test(int x, boolean b) {
        this();
    }

    Test(int x, int y) {
        this(1);
    }

    Test(int x, int y, int z) {
        this(false);
    }
}