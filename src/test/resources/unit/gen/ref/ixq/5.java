class Test {
    private Other[] ox;
    void main() {
        boolean y = ox[1].o.ox[2].o.bx[3];
    }
}

class Other {
    public boolean[] bx;
    public Other[] ox;
    public Other o;
}