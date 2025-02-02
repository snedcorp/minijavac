class Test {
    private Other o;
    void main() {
        f(1, 2);
        fstat();
        this.f(1, 2);
        Test.fstat();
        o.i(1);
        Other.istat(o, 3);
        o.o.i(4);
        getO().i(1);
    }

    private void f(int a, int b) {
        return;
    }

    private static void fstat() {
        return;
    }

    private Other getO() {
        return o;
    }
}

class Other {
    public Other o;
    public void i(int a) {return;}
    public static void istat(Other o, int b) {return;}
}