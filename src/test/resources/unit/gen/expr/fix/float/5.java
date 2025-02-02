class Test {
    void main(float p) {
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
    static float x;
    static Another a;
}

class Another {
    static float x;
}