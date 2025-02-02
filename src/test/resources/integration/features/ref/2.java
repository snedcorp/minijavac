class Test {
    int x;
    public static void main(String[] args) {
        int[][] x2 = new int[2][2];
        x2[0][0] = 1;
        x2[0][1] = 2;
        x2[1][0] = 3;
        x2[1][1] = 4;

        System.out.println(x2[0][0]);
        System.out.println(x2[0][1]);
        System.out.println(x2[1][0]);
        System.out.println(x2[1][1]);

        Test[][] t2 = new Test[2][2];
        Test t = new Test();
        t2[0][0] = t;
        t2[0][0].x = x2[1][1] * 2;
        System.out.println(t.x);
    }
}