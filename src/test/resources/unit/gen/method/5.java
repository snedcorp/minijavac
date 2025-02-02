class Test {
    public int[] arrpub() {
        return new int[5];
    }

    private boolean[] arrpriv() {
        return new boolean[5];
    }

    public static Other[] arrpubstat() {
        return new Other[5];
    }

    private static Other[] arrprivstat() {
        return new Other[5];
    }

    Other[] arr() {
        return new Other[5];
    }

    static Other[] arrstat() {
        return new Other[5];
    }
}

class Other {}