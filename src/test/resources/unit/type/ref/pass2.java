class Test {
    void main() {
        int[][] i2 = new int[2][2];
        Test[][][] t3 = new Test[2][3][4];
        int[] i1 = i2[1];
        int i = i2[1][1];
        Test[][] t2 = t3[1];
        Test[] t1 = t3[2][1];
        Test t = t3[1][2][3];
    }
}