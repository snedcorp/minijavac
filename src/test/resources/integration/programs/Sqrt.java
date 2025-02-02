class Sqrt {

    int sqrt(int x) {
        if (x <= 1) return x;
        return sqrt(0, x/2, x);
    }

    private int sqrt(int left, int right, int target) {
        if (right < left) return right;
        int mid = left + ((right - left) / 2);
        int midSq = mid * mid;
        if (target == midSq) return mid;
        else if (target < midSq) return sqrt(left, mid-1, target);
        return sqrt(mid+1, right, target);
    }

    public static void main(String[] args) {
        Sqrt sqrt = new Sqrt();
        System.out.println(sqrt.sqrt(0)); // 0
        System.out.println(sqrt.sqrt(1)); // 1
        System.out.println(sqrt.sqrt(2)); // 1
        System.out.println(sqrt.sqrt(3)); // 1
        System.out.println(sqrt.sqrt(4)); // 2
        System.out.println(sqrt.sqrt(5)); // 2
        System.out.println(sqrt.sqrt(6)); // 2
        System.out.println(sqrt.sqrt(7)); // 2
        System.out.println(sqrt.sqrt(8)); // 2
        System.out.println(sqrt.sqrt(9)); // 3
    }
}