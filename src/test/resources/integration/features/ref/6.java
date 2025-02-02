class Test {
    int x;
    public static void main(String[] args) {
        int[] ix = new int[]{1, 2, 3};
        boolean[] bx = new boolean[]{true, false};

        System.out.println(ix[0]);
        System.out.println(ix[1]);
        System.out.println(ix[2]);
        System.out.println(bx[0]);
        System.out.println(bx[1]);

        Test t1 = new Test();
        t1.x = 1;
        Test t2 = new Test();
        t2.x = 2;
        Test[] tx = new Test[]{t1, t2};

        System.out.println(tx[0].x);
        System.out.println(tx[1].x);
    }
}