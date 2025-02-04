class Test {

    int noCont(int n, boolean b) {
        int res = 0;
        for (int i=0; i<n; i++) {
            res++;
            if (b) break;
            return ++res;
        }
        return res;
    }

    int cont(int n, boolean b) {
        int res = 0;
        for (int i=0; i<n; i++) {
            res++;
            if (b) continue;
            return ++res;
        }
        return res;
    }

    public static void main(String[] args) {
        Test t = new Test();
        System.out.println(t.noCont(10, true)); // 1
        System.out.println(t.noCont(10, false)); // 2

        System.out.println(t.cont(10, true)); // 10
        System.out.println(t.cont(10, false)); // 2
    }
}