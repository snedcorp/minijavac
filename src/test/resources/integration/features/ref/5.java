class Test {
    public static void main(String[] args) {
        int[] ix = new int[2];
        int[][] ix2 = new int[3][2];
        ix2[0] = new int[2];
        System.out.println(ix.length);
        System.out.println(ix2.length);
        System.out.println(ix2[0].length);
    }
}