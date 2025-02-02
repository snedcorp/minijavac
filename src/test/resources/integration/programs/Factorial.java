class Factorial {

    static int fact(int n) {
        if (n == 0) return 1;
        return n * fact(n-1);
    }

    public static void main(String[] args) {
        System.out.println(fact(0)); // 1
        System.out.println(fact(1)); // 1
        System.out.println(fact(2)); // 2
        System.out.println(fact(3)); // 6
        System.out.println(fact(4)); // 24
        System.out.println(fact(5)); // 120
        System.out.println(fact(6)); // 720
    }
}