class Test {
    private Other o;
    void main() {
        int x = o.ostat.i;
    }
}

class Other {
    public int i;
    public static Other ostat;
}