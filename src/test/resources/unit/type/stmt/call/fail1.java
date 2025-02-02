class Test {
    public void main() {
        f(1);
        Other.f(1);
        g();
        g(1, true, 2);
    }

    public void f() {}

    public void g(int x, boolean y) {}
}

class Other {
    public static void f() {}
}