class HouseRobber {

    int[] nums;

    HouseRobber(int[] nums) {
        this.nums = nums;
    }

    int rob() {
        return rob(nums.length-1);
    }

    private int rob(int n) {
        if (n == 0) return nums[0];
        if (n == 1) return nums[0] > nums[1] ? nums[0] : nums[1];

        int robVal = rob(n-2) + nums[n];
        int noRobVal = rob(n-1);

        return robVal > noRobVal ? robVal : noRobVal;
    }

    int robIter() {
        if (nums.length == 1) return nums[0];

        int prev2 = nums[0];
        int prev = nums[0] > nums[1] ? nums[0] : nums[1];
        if (nums.length == 2) return prev;

        for (int i=2; i<nums.length; i++) {
            int rob = prev2 + nums[i];
            int noRob = prev;
            int val = rob > noRob ? rob : noRob;
            prev2 = prev;
            prev = val;
        }

        return prev;
    }

    public static void main(String[] args) {
        HouseRobber robber1 = new HouseRobber(new int[]{1, 2, 3, 1});
        System.out.println(robber1.rob()); // 4
        System.out.println(robber1.robIter()); // 4

        HouseRobber robber2 = new HouseRobber(new int[]{2, 7, 9, 3, 1});
        System.out.println(robber2.rob()); // 12
        System.out.println(robber2.robIter()); // 12
    }
}