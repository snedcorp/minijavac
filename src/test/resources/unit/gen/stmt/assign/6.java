class Test {
    private int i;
    private Other o;

    void main() {
        this.i = 1;
        Other.istat = 2;
        o.i = 3;
        o.o.i = 4;
    }
}

class Other {
    public int i;
    public static int istat;
    public Other o;
}