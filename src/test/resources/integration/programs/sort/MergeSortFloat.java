class MergeSortFloat {

    float[] arr;

    public MergeSortFloat(float[] arr) {
        this.arr = arr;
    }

    void sort() {
        float[] buffer = new float[arr.length];
        sort(0, arr.length-1, buffer);
    }

    private void sort(int left, int right, float[] buffer) {
        if (left == right) return;

        int mid = left + ((right - left) / 2);
        sort(left, mid, buffer);
        sort(mid+1, right, buffer);

        int leftIx = left;
        int rightIx = mid+1;

        int added = 0;

        while (leftIx <= mid || rightIx <= right) {
            float leftVal = leftIx <= mid ? arr[leftIx] : 100.0;
            float rightVal = rightIx <= right ? arr[rightIx] : 100.0;

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
        float[] arr = new float[]{4.3, 8.0, 3.8, 6.3, 1.1, 2.4, 5.3, 7.9, 6.4};

        MergeSortFloat sorter = new MergeSortFloat(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}