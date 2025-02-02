class Test {
    Other o;
    int x;
    void main() {
        x -= 2;
        o.x >>= 3;
        o.t.x &= 4;
    }
}

class Other {
    int x;
    Test t;
}