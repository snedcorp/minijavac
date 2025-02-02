class Test {
    private int[] ix;
    private Other o;

    void main() {
        this.ix[0] = 1;
        Other.ixstat[1] = 2;
        o.ix[2] = 3;
        o.ox[3].ix[4] = 4;
    }
}

class Other {
    public int[] ix;
    public static int[] ixstat;
    public Other[] ox;
}