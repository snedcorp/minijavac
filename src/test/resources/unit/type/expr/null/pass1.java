class Test {
    void main() {
        Test t = null;
        Test t2 = new Test();
        t2 = null;
        int[] arr = null;
        int[] arr2 = new int[3];
        arr2 = null;
        get(null);
        if (arr2 == null) {
            get(null);
        }
    }

    int get(Test t) {
        return 1;
    }
}