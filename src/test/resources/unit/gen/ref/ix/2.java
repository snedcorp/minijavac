class Test {
    static void main(boolean b) {
        int[][] i2 = new int[0][1];
        int i = i2[1][2];
        int[] i1 = i2[2];
        i2[3] = i1;
        i1 = i2[3];
        i2[4][5] = 3;
        return;
    }
}