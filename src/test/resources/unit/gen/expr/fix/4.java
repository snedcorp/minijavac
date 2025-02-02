class Test {
    Other o;
    void main(int p) {
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
    int x;
    Other o;
}