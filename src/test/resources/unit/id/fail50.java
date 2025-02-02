class Test {
    void main() {
        int x = f(1);
    }

    int f(boolean i) {
        return 1;
    }

    int f(Test[] t) {
        return 1;
    }

    int f(int i, int i2) {
        return 1;
    }
}