class Test {
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