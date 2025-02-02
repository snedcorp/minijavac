class Test {
    void main() {
        getO(true).f(1, 2);
    }

    Other getO(boolean b) {
        return new Other();
    }
}

class Other {
    public void f(int i, int j) {}
}