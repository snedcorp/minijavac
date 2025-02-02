class Test {
    public int i() {
        return false;
    }

    public boolean b() {
        return 1;
    }

    public int[] bx() {
        return new boolean[5];
    }

    public boolean[] ix() {
        return new int[5];
    }

    public Test f() {
        return new Other();
    }

    public Test[] fx() {
        return new Other[5];
    }
}

class Other {}