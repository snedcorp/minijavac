class Test {
    Other o;
    void main() {
        get();
        get(1);
        o.get(true, 1);
    }

    void set() {}
}

class Other {
    void set() {}
}