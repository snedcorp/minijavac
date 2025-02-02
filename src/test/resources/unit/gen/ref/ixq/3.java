class Test {
    void main() {
        Other o = new Other();
        int y = o.ox[2].ix[3];
    }
}

class Other {
    public int[] ix;
    public Other[] ox;
}