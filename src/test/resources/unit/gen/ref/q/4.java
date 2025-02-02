class Test {
    Other o;
    void main() {
        int x = o.i;
        int y = o.o.i;
    }
}

class Other {
    public int i;
    public Other o;
}