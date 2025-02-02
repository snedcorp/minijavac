class Test {
    void main() {
        Other o = new Other();
        int y = o.o.i;
    }
}

class Other {
    public int i;
    public Other o;
}