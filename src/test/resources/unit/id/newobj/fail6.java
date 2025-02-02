class Test {
    void main() {
        Other o = new Other(1);
    }
}

class Other {
    Other(boolean i) {}

    Other(Test[] t) {}

    Other(int i, int i2) {}
}