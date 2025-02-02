class Test {
    void main() {
        int x = f(1);
    }

    int f() {}

    int f(boolean b) {
        return 1;
    }

    int f(int i, int i2) {
        return 1;
    }
}