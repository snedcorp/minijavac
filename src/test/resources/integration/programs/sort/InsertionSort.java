class InsertionSort {

    int[] arr;

    public InsertionSort(int[] arr) {
        this.arr = arr;
    }

    void sort() {
        if (arr.length == 1) return;

        for (int i=1; i<arr.length; i++) {
            int val = arr[i];
            int j = i-1;
            while (j >= 0 && arr[j] > val) {
                arr[j+1] = arr[j];
                j--;
            }
            arr[j+1] = val;
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        InsertionSort sorter = new InsertionSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}