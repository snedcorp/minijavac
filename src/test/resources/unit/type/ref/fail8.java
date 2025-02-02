class Test {
    void main() {
        int[][][] i3 = new int[2][2][2];
        i3 = i3[1];
        int[][] i1 = i3[1][2];
        boolean x = i3[1][2][3];
    }
}