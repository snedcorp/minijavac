class Test {
    boolean b;
    int i;
    void main() {
        int x = this.getO().b;
        x = this.getO().getT().b;
        boolean y = this.getO().i;
        y = this.getO().getT().i;
    }

    Other getO() {
        return new Other();
    }
}

class Other {
    boolean b;
    int i;

    Test getT() {
        return new Test();
    }
}