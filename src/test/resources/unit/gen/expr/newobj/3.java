class Test {

    int x;
    boolean y;

    Test(int x) {
        this.x = x;
        this.y = false;
    }

    Test(int x, boolean y) {
        this.x = x;
        this.y = y;
    }
    static void main() {
        Test t1 = new Test(1);
        Test t2 = new Test(3, true);
    }
}