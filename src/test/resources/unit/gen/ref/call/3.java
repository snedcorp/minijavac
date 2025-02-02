class Test {
    private Other o;
    void main() {
        int x = this.f(1);
        int y = o.i(2);
        int z = Other.istat(3, 4);
        int zz = o.o.i(5);
    }

    private int f(int a) {
        return a;
    }
}

class Other {
    public Other o;
    public int i(int a) {return 1;}
    public static int istat(int a, int b) {return 1;}
}