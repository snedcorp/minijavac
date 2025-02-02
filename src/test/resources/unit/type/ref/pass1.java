class Test {
    int p;
    int[] px;

    public void main() {
        int x = 1;
        x = 1 + x + Other.x;

        boolean y = true;
        y = y || Other.b();

        Other o = new Other();
        x = o.z.p - 3;

        x = px[1] + o.z.px[2];

        y = 1 < 2 || b();

        x = this.getO().getT().px[1];
        y = this.getO().zz[1].getO().getT().b();
    }

    public boolean b() {
        return true;
    }

    Other getO() {
        return new Other();
    }
}

class Other {

    public static int x;
    public static boolean y;
    public Test z;
    public Test[] zz;

    public static boolean b() {
        return y;
    }

    public Test getT() {
        return z;
    }
}