class CountingSort {

    int[] arr;
    int maxVal;

    public CountingSort(int[] arr, int maxVal) {
        this.arr = arr;
        this.maxVal = maxVal;
    }

    int[] sort() {
        if (arr.length == 1) return arr;

        int[] counts = new int[maxVal+1];

        for (int i=0; i<arr.length; i++) {
            counts[arr[i]] += 1;
        }

        int nextPos = 0;
        for (int i=0; i<counts.length; i++) {
            int newPos = nextPos;
            nextPos += counts[i];
            counts[i] = newPos;
        }

        int[] startPos = counts;

        int[] res = new int[arr.length];

        for (int i=0; i<arr.length; i++) {
            res[startPos[arr[i]]++] = arr[i];
        }

        return res;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 0, 6, 7, 8, 3, 6, 1, 2, 1, 1, 4, 5, 3, 7};

        CountingSort sorter = new CountingSort(arr, 8);
        int[] res = sorter.sort();

        for (int i=0; i<res.length; i++) {
            System.out.println(res[i]);
        }
    }
}