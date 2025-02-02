class Test {
    public static void main(String[] args) {
        System.out.println(mult(3));
        System.out.println(mult(3, 4));
        System.out.println(logic(false));
        System.out.println(logic(4, 3));
    }

    static int mult(int i) {
        return i*i;
    }

    static int mult(int i, int j) {
        return i*j;
    }

    static boolean logic(boolean i) {
        return !i;
    }

    static boolean logic(int i, int j) {
        return i < j;
    }
}