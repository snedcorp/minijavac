class Test {
    public Other clspub() {
        return new Other();
    }

    private Other clspriv() {
        return new Other();
    }

    public static Other clspubstat() {
        return new Other();
    }

    private static Other clsprivstat() {
        return new Other();
    }

    Other cls() {
        return new Other();
    }

    static Other clsstat() {
        return new Other();
    }
}

class Other {}