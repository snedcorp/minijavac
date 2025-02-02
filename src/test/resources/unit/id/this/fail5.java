class Test {
    Test(boolean i) {}

    Test(Test[] t) {}

    Test(int i, int i2) {}

    Test(boolean b, boolean b2) {
        this(1);
    }
}

class Other {
}