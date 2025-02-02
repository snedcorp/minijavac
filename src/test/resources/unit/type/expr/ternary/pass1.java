class Test {
    boolean b;
    void main() {
        int x = b ? 1 : 0;
        boolean y = !b ? false : true;
        x = getB() ? 10 + 5 : 30;
    }

    boolean getB() {
        return true;
    }
}