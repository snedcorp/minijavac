class Test {
    static void main() {
        Other o1 = new Other(1);
        Other o2 = new Other(3, true);
    }
}

class Other {
    int x;
    boolean y;

    Other(int x) {
        this.x = x;
        this.y = false;
    }

    Other(int x, boolean y) {
        this.x = x;
        this.y = y;
    }
}