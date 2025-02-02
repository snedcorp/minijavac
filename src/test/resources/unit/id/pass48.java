class Test {
    Other o;
    void main() {
        f(1);
        o.f(1);
    }
    void f(int i) {}
}

class Other {
    public void f(int i) {}
}