class Test {
    public void main() {
        f();
        Other.f();
        g(1, false);
        h(new Other(), new Other[5]);
    }

    public void f() {}

    public void g(int x, boolean y) {}

    public void h(Other o, Other[] ox) {}
}

class Other {
    public static void f() {}
}