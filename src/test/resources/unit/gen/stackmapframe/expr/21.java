class Test {

    int x;

    void main(boolean b) {
        int v = get(++x, !b);
    }

    int get(int i, boolean b) {
        return 1;
    }
}