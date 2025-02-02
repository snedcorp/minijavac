class Test {
    void main() {
        getO(1).f(1, true);
        getO(true).f(1, true);
    }

    Other getO(boolean b) {
        return new Other();
    }
}

class Other {
    public void f(int i, int j) {}
}