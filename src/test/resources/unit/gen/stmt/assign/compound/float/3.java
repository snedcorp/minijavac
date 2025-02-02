class Test {
    Other o;
    float x;
    void main() {
        x -= 2.2;
        o.x += 3.3;
        o.t.x /= 4.4;
    }
}

class Other {
    float x;
    Test t;
}