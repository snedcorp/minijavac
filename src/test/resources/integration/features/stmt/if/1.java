class Test {
    public static void main(String[] args) {
        p(0);
        p(1);
        p(2);
        p(3);
    }

    static void p(int i) {
        if (i == 0) System.out.println(0);
        else if (i == 1) System.out.println(1);
        else if (i == 2) System.out.println(2);
        else System.out.println(3);
    }
}