class MergeSort {

    int[] arr;

    public MergeSort(int[] arr) {
        this.arr = arr;
    }

    void sort() {
        int[] buffer = new int[arr.length];
        sort(0, arr.length-1, buffer);
    }

    private void sort(int left, int right, int[] buffer) {
        if (left == right) return;

        int mid = left + ((right - left) / 2);
        sort(left, mid, buffer);
        sort(mid+1, right, buffer);

        int leftIx = left;
        int rightIx = mid+1;

        int added = 0;

        while (leftIx <= mid || rightIx <= right) {
            int leftVal = leftIx <= mid ? arr[leftIx] : 100;
            int rightVal = rightIx <= right ? arr[rightIx] : 100;

            if (leftVal <= rightVal) {
                buffer[added++] = leftVal;
                leftIx++;
            } else {
                buffer[added++] = rightVal;
                rightIx++;
            }
        }

        for (int i=left; i<=right; i++) {
            arr[i] = buffer[i-left];
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        MergeSort sorter = new MergeSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}