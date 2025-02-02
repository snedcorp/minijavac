class Test {
    public static void main(String[] args) {
        int[] arr = new int[5];
        int x = arr[0];
        int y = Other.arr[1 + 2 * 3];
    }
}

class Other {
    static int[] arr;
}