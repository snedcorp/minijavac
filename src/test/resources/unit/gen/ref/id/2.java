class Test {
    static void main() {
        int a = 0;
        boolean b = true;
        boolean c = false;
        Test t = new Test();
        Test t2 = new Test();
        int d = 5;
        d = a;
        b = c;
        c = b;
        t2 = t;
        Test t3 = t2;
        a = d;
    }
}