class Test {
    private int[] ix;
    private boolean[] bx;
    private Test[] tx;
    private static int[] ixstat;

    void main() {
        int[] lix = new int[3];
        boolean[] lbx = new boolean[3];
        Test[] ltx = new Test[3];
        ix[0] = 2;
        lix[0] = 2;
        bx[1] = true;
        lbx[1] = false;
        tx[2] = ltx[2];
        ltx[2] = tx[2];
        ixstat[3] = ix[3];
        ix[3] = ixstat[3];
    }
}