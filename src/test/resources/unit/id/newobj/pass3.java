class Test {
    void main() {
        Other o = new Other();
        Other o2 = new Other(2);
        Other o3 = new Other(false);
        Other o4 = new Other(5, true);
    }
}

class Other {

    Other() {}

    Other(int x) {}

    Other(boolean b) {}

    Other(int x, boolean b) {}
}