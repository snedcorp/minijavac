class Test {
    Other o;
    int x;
    void main() {
        x = this.i();
        x = i();
        x = istat();
        x = Test.istat();
        x = Other.oistat();
        x = o.oi();
        x = o.ot().i();
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