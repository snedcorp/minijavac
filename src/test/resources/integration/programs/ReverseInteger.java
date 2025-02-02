class ReverseInteger {
    int reverse(int x) {
        boolean isNeg = x < 0;
        int res = 0;
        while (x != 0) {
            int digit = x % 10;
            digit = digit < 0 ? -digit : digit;
            if ((2147483647 - digit) / 10 < res) return 0;
            res = (res*10) + digit;
            x /= 10;
        }

        if (isNeg) {
            res = -res;
        }

        return res;
    }

    public static void main(String[] args) {
        ReverseInteger reverser = new ReverseInteger();
        System.out.println(reverser.reverse(123)); // 321
        System.out.println(reverser.reverse(-123)); // -321
        System.out.println(reverser.reverse(120)); // 21
        System.out.println(reverser.reverse(1534236469)); // 0
        System.out.println(reverser.reverse(1463847412)); // 2147483641
    }
}