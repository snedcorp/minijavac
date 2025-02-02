class Test {
    void main() {
        int x = f();
        int y = fstat();
    }

    private int f() {
        return 1;
    }

    private static int fstat() {
        return 1;
    }
}