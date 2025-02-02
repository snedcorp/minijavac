class Test {
    int[] fix;
    void main(int p) {
        int[] ix = new int[2];
        int[][] ix2 = new int[2][2];
        p = ++ix[1];
        p = ++ix2[1][2];
        p = ++fix[1];
        p = --ix[1];
        p = ix[1]++;
        p = ix[1]--;
        ++ix[1];
        --ix[1];
        ix[1]++;
        ix[1]--;
        Other.fix[1]--;
    }
}

class Other {
    static int[] fix;
}