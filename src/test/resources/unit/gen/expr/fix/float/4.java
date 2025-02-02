class Test {
    Other o;
    void main(float p) {
        p = ++o.x;
        p = ++o.o.x;
        p = --o.x;
        p = o.x++;
        p = o.x--;
        ++o.x;
        --o.x;
        o.x++;
        o.x--;
        o.o.x--;
    }
}

class Other {
    float x;
    Other o;
}