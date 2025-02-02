class Test {
    int x;

    void main() {
        int x = getB()[1];
        boolean y = getT()[0].x;
    }

    boolean[] getB() {
        return new boolean[2];
    }

    Test[] getT() {
        return new Test[2];
    }
}