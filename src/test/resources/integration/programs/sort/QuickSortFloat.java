class QuickSortFloat {

    float[] arr;

    public QuickSortFloat(float[] arr) {
        this.arr = arr;
    }

    void sort() {
        quickSort(0, arr.length-1);
    }

    private void quickSort(int left, int right) {
        if (right <= left) return;
        int p = partition(left, right);
        quickSort(left, p-1);
        quickSort(p+1, right);
    }

    private int partition(int left, int right) {
        int firstHigherIx = left;
        float pivot = arr[right];
        for (int i=left; i<right; i++) {
            if (arr[i] < pivot) {
                swap(i, firstHigherIx);
                firstHigherIx++;
            }
        }
        swap(right, firstHigherIx);
        return firstHigherIx;
    }

    private void swap(int i, int j) {
        float tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void main(String[] args) {
        float[] arr = new float[]{4.3, 8.0, 3.8, 6.3, 1.1, 2.4, 5.3, 7.9, 6.4};

        QuickSortFloat sorter = new QuickSortFloat(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}