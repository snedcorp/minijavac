class Test {
    void main() {
        int x = f(1);
        int y = fstat(2, 3);
    }

    private int f(int a) {
        return a;
    }

    private static int fstat(int a, int b) {
        return a + b;
    }
}