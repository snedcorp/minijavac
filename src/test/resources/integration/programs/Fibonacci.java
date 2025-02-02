class Fibonacci {

    static int fib(int n) {
        if (n <= 1) return n;
        return fib(n-1) + fib(n-2);
    }

    static int fibIter(int n) {
        if (n <= 1) return n;
        int twoBefore = 0;
        int oneBefore = 1;
        for (int i=2; i<=n; i++) {
            int val = twoBefore + oneBefore;
            twoBefore = oneBefore;
            oneBefore = val;
        }
        return oneBefore;
    }

    public static void main(String[] args) {
        System.out.println(fib(0)); // 0
        System.out.println(fib(1)); // 1
        System.out.println(fib(2)); // 1
        System.out.println(fib(3)); // 2
        System.out.println(fib(4)); // 3
        System.out.println(fib(5)); // 5
        System.out.println(fib(6)); // 8
        System.out.println(fib(7)); // 13

        System.out.println(fibIter(0)); // 0
        System.out.println(fibIter(1)); // 1
        System.out.println(fibIter(2)); // 1
        System.out.println(fibIter(3)); // 2
        System.out.println(fibIter(4)); // 3
        System.out.println(fibIter(5)); // 5
        System.out.println(fibIter(6)); // 8
        System.out.println(fibIter(7)); // 13
    }
}