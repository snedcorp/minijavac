class Test {
    public static void main(String[] args) {
        int i = 1;
        boolean b = true;
        int[] ix = new int[5];
        boolean[] bx = new boolean[5];
        Test f = new Test();
        Test[] fix = new Test[5];

        i = false;
        b = 2;
        ix = new boolean[10];
        bx = new int[10];
        f = new Other();
        fix = new Other[10];
    }
}

class Other {}