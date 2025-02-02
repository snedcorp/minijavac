class Test {

    Test[] tx;
    void main() {
        int x = get()[1];
        int y = tx[0].getTx()[1].get()[2];
    }

    int[] get() {
        return new int[2];
    }

    Test[] getTx() {
        return tx;
    }
}