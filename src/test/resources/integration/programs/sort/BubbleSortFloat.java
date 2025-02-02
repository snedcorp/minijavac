class BubbleSortFloat {

    float[] arr;

    public BubbleSortFloat(float[] arr) {
        this.arr = arr;
    }

    void sort() {
        for (int i=0; i<arr.length-1; i++) {
            for (int j=0; j<arr.length-1; j++) {
                if (arr[j] > arr[j+1]) {
                    float tmp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = tmp;
                }
            }
        }
    }

    public static void main(String[] args) {
        float[] arr = new float[]{4.3, 8.0, 3.8, 6.3, 1.1, 2.4, 5.3, 7.9, 6.4};

        BubbleSortFloat sorter = new BubbleSortFloat(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}