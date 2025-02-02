class Test {
    void main() {
        Other o = new Other(1);
        Other o2 = new Other(1, true);
    }
}

class Other {

    Other(boolean a) {}

    Other(int a, int b) {}
}