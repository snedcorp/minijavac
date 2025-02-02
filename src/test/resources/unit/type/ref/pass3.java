class Test {
    int x;

    void main() {
        boolean x = getB()[1];
        int y = getT()[0].x;
    }

    boolean[] getB() {
        return new boolean[2];
    }

    Test[] getT() {
        return new Test[2];
    }
}