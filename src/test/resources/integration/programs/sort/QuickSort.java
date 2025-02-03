class QuickSort {

    int[] arr;

    public QuickSort(int[] arr) {
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
        int pivot = arr[right];
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
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        QuickSort sorter = new QuickSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}