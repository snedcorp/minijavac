class Test {
    public static void main(String[] args) {
        System.out.println(test(true, false, true));
        System.out.println(test(true, false, false));
        System.out.println(test(false, false, false));
    }

    static boolean test(boolean a, boolean b, boolean c) {
        return and(a, or(b && c, !c));
    }

    static boolean and(boolean a, boolean b) {
        return a && b;
    }

    static boolean or(boolean a, boolean b) {
        return a || b;
    }
}