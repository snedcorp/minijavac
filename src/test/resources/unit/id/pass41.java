class Test {
    public static void main(String[] args) {
        A[] a = new A[5];
        a[0].b.c[0].d.e = 1;
    }
}

class A {
    B b;
}

class B {
    C[] c;
}

class C {
    D d;
}

class D {
    int e;
}