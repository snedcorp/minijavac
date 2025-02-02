class BinarySearch {

    int[] arr;

    BinarySearch(int[] arr) {
        this.arr = arr;
    }

    int search(int n) {
        return search(0, arr.length-1, n);
    }

    private int search(int left, int right, int target) {
        if (right < left) return -1;
        int mid = left + ((right - left) / 2);
        if (target == arr[mid]) return mid;
        else if (target < arr[mid]) return search(left, mid, target);
        return search(mid, right, target);
    }

    public static void main(String[] args) {
        int[] arr = new int[]{1, 3, 4, 6, 8, 10, 11, 12, 13, 15, 16, 18, 20};

        BinarySearch searcher = new BinarySearch(arr);

        System.out.println(searcher.search(0)); // -1
        System.out.println(searcher.search(1)); // 0
        System.out.println(searcher.search(2)); // -1
        System.out.println(searcher.search(3)); // 1
        System.out.println(searcher.search(4)); // 2
        System.out.println(searcher.search(5)); // -1
        System.out.println(searcher.search(6)); // 3
        System.out.println(searcher.search(7)); // -1
        System.out.println(searcher.search(8)); // 4
        System.out.println(searcher.search(9)); // -1
        System.out.println(searcher.search(10)); // 5
        System.out.println(searcher.search(11)); // 6
        System.out.println(searcher.search(12)); // 7
        System.out.println(searcher.search(13)); // 8
        System.out.println(searcher.search(14)); // -1
        System.out.println(searcher.search(15)); // 9
        System.out.println(searcher.search(16)); // 10
        System.out.println(searcher.search(17)); // -1
        System.out.println(searcher.search(18)); // 11
        System.out.println(searcher.search(19)); // -1
        System.out.println(searcher.search(20)); // 12
        System.out.println(searcher.search(21)); // -1
    }
}