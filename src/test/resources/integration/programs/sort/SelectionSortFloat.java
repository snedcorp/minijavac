class SelectionSortFloat {

    float[] arr;

    public SelectionSortFloat(float[] arr) {
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
                float tmp = arr[i];
                arr[i] = arr[minIx];
                arr[minIx] = tmp;
            }
        }
    }

    public static void main(String[] args) {
        float[] arr = new float[]{4.3, 8.0, 3.8, 6.3, 1.1, 2.4, 5.3, 7.9, 6.4};

        SelectionSortFloat sorter = new SelectionSortFloat(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}