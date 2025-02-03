class KthLargestElement {

    int findKthLargest(int[] nums, int k) {
        MinHeap minHeap = new MinHeap(k);

        for (int i=0; i<nums.length; i++) {
            int num = nums[i];
            if (minHeap.size() < k) {
                minHeap.offer(num);
                continue;
            }

            if (num >= minHeap.peek()) {
                minHeap.poll();
                minHeap.offer(num);
            }
        }

        return minHeap.poll();
    }

    public static void main(String[] args) {
        KthLargestElement finder = new KthLargestElement();

        int[] arr = new int[]{3, 2, 1, 5, 6, 4};

        System.out.println(finder.findKthLargest(arr, 1)); // 6
        System.out.println(finder.findKthLargest(arr, 2)); // 5
        System.out.println(finder.findKthLargest(arr, 3)); // 4
        System.out.println(finder.findKthLargest(arr, 4)); // 3
        System.out.println(finder.findKthLargest(arr, 5)); // 2
        System.out.println(finder.findKthLargest(arr, 6)); // 1
    }
}