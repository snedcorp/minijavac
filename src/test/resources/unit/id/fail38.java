class Test {
    int i;
    Other o;
    void main() {
        int r = this.i();
        int r2 = i();
        int r3 = o.i(1);
        int r4 = o.im(1, 2);
        int r5 = o.tm().p;
    }
}

class Other {

    private int im(int i, int i2) {
        return 1;
    }

    Test tm() {
        return new Test();
    }
}