class InsertionSortFloat {

    float[] arr;

    public InsertionSortFloat(float[] arr) {
        this.arr = arr;
    }

    void sort() {
        if (arr.length == 1) return;

        for (int i=1; i<arr.length; i++) {
            float val = arr[i];
            int j = i-1;
            while (j >= 0 && arr[j] > val) {
                arr[j+1] = arr[j];
                j--;
            }
            arr[j+1] = val;
        }
    }

    public static void main(String[] args) {
        float[] arr = new float[]{4.3, 8.0, 3.8, 6.3, 1.1, 2.4, 5.3, 7.9, 6.4};

        InsertionSortFloat sorter = new InsertionSortFloat(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}