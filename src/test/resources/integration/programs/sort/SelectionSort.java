class SelectionSort {

    int[] arr;

    public SelectionSort(int[] arr) {
        this.arr = arr;
    }

    void sort() {
        if (arr.length == 1) return;

        for (int i=0; i<arr.length; i++) {
            int minIx = i;
            for (int j=i+1; j<arr.length; j++) {
                if (arr[j] < arr[minIx]) minIx = j;
            }
            if (minIx != i) {
                int tmp = arr[i];
                arr[i] = arr[minIx];
                arr[minIx] = tmp;
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        SelectionSort sorter = new SelectionSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}