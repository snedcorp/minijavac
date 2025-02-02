class Test {
    void main() {
        getO(getB(false)).f(1+2, getI(1));
    }

    boolean getB(boolean b) {
        return !b;
    }

    int getI(int i) {
        return i + 1;
    }

    Other getO(boolean b) {
        return new Other();
    }
}

class Other {
    public void f(int i, int j) {}
}