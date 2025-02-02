class Test {
    int p;
    int[] px;

    public void main() {
        int x = Other.b();
        boolean y = i();
    }

    public int i() {
        return 1;
    }
}

class Other {
    public static boolean y;

    public static boolean b() {
        return y;
    }
}