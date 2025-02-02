class Test {
    Test() {}

    Test(int i, int i2) {}

    Test(int i, int i2, int i3) {
        this(i);
    }
}

class Other {
    Other() {}

    Other(int i, int i2) {}
}