class Test {
    public static void main(String[] args) {
        A a = new A();
        a.b[0].c.d = 1;
    }
}

class A {
    B[] b;
}

class B {
    C c;
}

class C {
    void d() {}
}