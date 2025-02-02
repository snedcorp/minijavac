class Test {
    int x;
    public static void main(String[] args) {
        int[][] ix2 = new int[][]{{1, 2}, {3, 4}};
        boolean[][] bx2 = new boolean[][]{{true, false}, {true, false}};

        System.out.println(ix2[0][0]);
        System.out.println(ix2[0][1]);
        System.out.println(ix2[1][0]);
        System.out.println(ix2[1][1]);
        System.out.println(bx2[0][0]);
        System.out.println(bx2[0][1]);
        System.out.println(bx2[1][0]);
        System.out.println(bx2[1][1]);

        Test t1 = new Test();
        t1.x = 1;
        Test t2 = new Test();
        t2.x = 2;
        Test t3 = new Test();
        t3.x = 3;
        Test t4 = new Test();
        t4.x = 4;

        Test[][] tx2 = new Test[][]{{t1, t2}, {t3, t4}};

        System.out.println(tx2[0][0].x);
        System.out.println(tx2[0][1].x);
        System.out.println(tx2[1][0].x);
        System.out.println(tx2[1][1].x);
    }
}