class Test {
    private Other o;
    void main() {
        int x = getO().i(2);
        getO().x = x;
        x = Other.tstat(1, 2).getO().x;
        Other lo = getO().tx[1].getO();
    }

    Other getO() {
        return o;
    }
}

class Other {
    public int x;
    public Test[] tx;
    public int i(int a) {return 1;}
    public static Test tstat(int a, int b) {return new Test();}
}