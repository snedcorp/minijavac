class Test {
    static void main(boolean b) {
        int[] ix = new int[2];
        int v = get(++ix[1], !b);
    }

    static int get(int i, boolean b) {
        return 1;
    }
}