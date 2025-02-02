class Test {
    int p;
    boolean b;
    int[] px;

    public void main() {
        int x = Other.b;
        boolean y = Other.p;

        Other o = new Other();
        y = o.z.p;

        this.b = px[1];
        Other.b = o.z.px[2];
    }
}

class Other {

    public static int p;
    public static boolean b;
    public Test z;
}