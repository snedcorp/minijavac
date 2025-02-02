class Test {
    Other o;
    int x;
    void main() {
        this.i();
        i();
        istat();
        Test.istat();
        Other.oistat();
        o.oi();
        o.ot().i();
    }

    int i() {
        return 1;
    }

    static int istat() {
        return 1;
    }
}

class Other {
    int oi() {
        return 1;
    }

    static int oistat() {
        return 1;
    }

    Test ot() {
        return new Test();
    }
}