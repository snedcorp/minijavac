class Test {
    static void main(boolean b) {
        int r = get(new Test(), !b);
    }

    static int get(Test t, boolean b) {
        return 1;
    }
}