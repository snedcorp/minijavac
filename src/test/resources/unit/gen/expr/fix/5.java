class Test {
    void main(int p) {
        p = ++Other.x;
        p = ++Other.a.x;
        p = --Other.x;
        p = Other.x++;
        p = Other.x--;
        ++Other.x;
        --Other.x;
        Other.x++;
        Other.x--;
        Other.a.x--;
    }
}

class Other {
    static int x;
    static Another a;
}

class Another {
    static int x;
}