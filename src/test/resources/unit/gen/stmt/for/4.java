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
}