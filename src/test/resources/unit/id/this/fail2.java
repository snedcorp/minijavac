class Test {
    Test() {}

    Test(int x) {
        x = 2;
        this();
    }
}