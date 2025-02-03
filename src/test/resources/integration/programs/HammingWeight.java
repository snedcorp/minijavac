class HammingWeight {

    int getWeight(int n) {
        int numOnes = 0;
        while (n > 0) {
            if (n % 2 == 1) numOnes++;
            n >>= 1;
        }
        return numOnes;
    }

    public static void main(String[] args) {
        HammingWeight hw = new HammingWeight();
        System.out.println(hw.getWeight(0)); // 0
        System.out.println(hw.getWeight(1)); // 1
        System.out.println(hw.getWeight(2)); // 1
        System.out.println(hw.getWeight(3)); // 2
        System.out.println(hw.getWeight(4)); // 1
        System.out.println(hw.getWeight(5)); // 2
        System.out.println(hw.getWeight(6)); // 2
        System.out.println(hw.getWeight(7)); // 3
        System.out.println(hw.getWeight(8)); // 1
        System.out.println(hw.getWeight(9)); // 2
        System.out.println(hw.getWeight(10)); // 2
        System.out.println(hw.getWeight(11)); // 3
        System.out.println(hw.getWeight(12)); // 2
        System.out.println(hw.getWeight(13)); // 3
        System.out.println(hw.getWeight(14)); // 3
        System.out.println(hw.getWeight(15)); // 4
        System.out.println(hw.getWeight(16)); // 1
    }
}