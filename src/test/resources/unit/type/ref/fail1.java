class Test {
    public static void main(String[] args) {
        int[] arr = new int[5];
        int[][] arr2 = new int[5][5];
        int x = arr[false];
        int y = Other.arr[1 >= 2];
        int x2 = arr2[1][false];
        int y2 = Other.arr2[2][true];
    }
}

class Other {
    static int[] arr;
    static int[][] arr2;
}