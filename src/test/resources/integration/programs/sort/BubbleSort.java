class BubbleSort {

    int[] arr;

    public BubbleSort(int[] arr) {
        this.arr = arr;
    }

    void sort() {
        for (int i=0; i<arr.length-1; i++) {
            for (int j=0; j<arr.length-1; j++) {
                if (arr[j] > arr[j+1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = tmp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 8, 3, 6, 1, 2, 5, 7};

        BubbleSort sorter = new BubbleSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}