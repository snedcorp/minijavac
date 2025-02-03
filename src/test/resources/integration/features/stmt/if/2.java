class Test {

    int val;

    Test(int val) {
        this.val = val;
    }

    int getI(boolean b, boolean c) {
        if (b) return 1;
        else if (c) return 2;
        return 3;
    }

    float getF(boolean b, boolean c) {
        if (b) return 1.1;
        else if (c) return 2.2;
        return 3.3;
    }

    Test getTest(boolean b, boolean c, Test t1, Test t2, Test t3) {
        if (b) return t1;
        else if (c) return t2;
        return t3;
    }

    public static void main(String[] args) {
        Test t = new Test(0);
        System.out.println(t.getI(true, true)); // 1
        System.out.println(t.getI(true, false)); // 1
        System.out.println(t.getI(false, true)); // 2
        System.out.println(t.getI(false, false)); // 3

        System.out.println(t.getF(true, true)); // 1.1
        System.out.println(t.getF(true, false)); // 1.1
        System.out.println(t.getF(false, true)); // 2.2
        System.out.println(t.getF(false, false)); // 3.3

        Test t1 = new Test(1);
        Test t2 = new Test(2);
        Test t3 = new Test(3);

        System.out.println(t.getTest(true, true, t1, t2, t3).val); // 1
        System.out.println(t.getTest(true, false, t1, t2, t3).val); // 1
        System.out.println(t.getTest(false, true, t1, t2, t3).val); // 2
        System.out.println(t.getTest(false, false, t1, t2, t3).val); // 3
    }
}