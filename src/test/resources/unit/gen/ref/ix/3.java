class Test {
    int p;
    void main() {
        int x = get()[0];
        int y = this.getT()[1].p;
    }

    int[] get() {
        return new int[2];
    }

    Test[] getT() {
        return new Test[2];
    }
}