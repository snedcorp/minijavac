class Test {
    public void a(int i) {}

    public void b(boolean b) {}

    public void c(Other o) {}

    public void d(int[] i) {}

    public int e(int i, boolean b, Other o, int[] ix, String s, Other[] ox) {
        return 1;
    }
}

class Other {}