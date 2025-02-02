class Test {
    Other o;
    void main() {
        int x = this.o.i;
        int y = this.o.o.i;
    }
}

class Other {
    public int i;
    public Other o;
}