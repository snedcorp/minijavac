class Test {
    static void main(boolean b) {
        int[][] i2 = new int[2][3];
        Test[][][] t3 = new Test[2][3][4];
        int[] i1 = i2[1];
        Test[][] t2 = t3[0];
        if (b) {
            b = false;
        }
        return;
    }
}