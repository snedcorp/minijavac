class Test {
    private static Other o;
    static void main() {
        int x = Test.o.i;
    }
}

class Other {
    public int i;
}