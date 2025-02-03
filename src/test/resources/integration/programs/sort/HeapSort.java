class HeapSort {

    int[] arr;
    int size;

    HeapSort(int[] arr) {
        this.arr = arr;
        this.size = arr.length;
    }

    void sort() {
        heapify();
        while (size > 1) {
            swap(0, size-1);
            size--;
            bubbleDown(0);
        }
    }

    private void heapify() {
        for (int i=arr.length-1; i>=0; i--) {
            bubbleDown(i);
        }
    }

    private void bubbleDown(int i) {
        while (i <= (size/2)-1) {
            int leftIx = (i*2) + 1;
            int rightIx = (i*2) + 2;

            int childIx = rightIx > size-1 ? leftIx : arr[leftIx] >= arr[rightIx] ? leftIx : rightIx;
            if (arr[childIx] <= arr[i]) break;

            swap(i, childIx);
            i = childIx;
        }
    }

    private void swap(int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    int size() {
        return size;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        HeapSort sorter = new HeapSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}