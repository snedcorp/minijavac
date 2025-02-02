class PascalsTriangle {

    int[] getNthRow(int n) {
        int[] prevRow = new int[]{1};

        for (int i=2; i<=n; i++) {
            int[] newRow = new int[i];
            newRow[0] = 1;
            for (int j=1; j<i-1; j++) {
                newRow[j] = prevRow[j-1] + prevRow[j];
            }
            newRow[i-1] = 1;
            prevRow = newRow;
        }

        return prevRow;
    }

    public static void main(String[] args) {
        PascalsTriangle pt = new PascalsTriangle();

        int[] res1 = pt.getNthRow(1);
        System.out.println(res1.length == 1); // true
        System.out.println(res1[0]); // 1

        int[] res2 = pt.getNthRow(2);
        System.out.println(res2.length == 2); // true
        System.out.println(res2[0]); // 1
        System.out.println(res2[1]); // 1

        int[] res3 = pt.getNthRow(3);
        System.out.println(res3.length == 3); // true
        System.out.println(res3[0]); // 1
        System.out.println(res3[1]); // 2
        System.out.println(res3[2]); // 1

        int[] res4 = pt.getNthRow(4);
        System.out.println(res4.length == 4); // true
        System.out.println(res4[0]); // 1
        System.out.println(res4[1]); // 3
        System.out.println(res4[2]); // 3
        System.out.println(res4[3]); // 1

        int[] res5 = pt.getNthRow(5);
        System.out.println(res5.length == 5); // true
        System.out.println(res5[0]); // 1
        System.out.println(res5[1]); // 4
        System.out.println(res5[2]); // 6
        System.out.println(res5[3]); // 4
        System.out.println(res5[4]); // 1
    }
}