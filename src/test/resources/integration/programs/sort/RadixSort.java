class RadixSort {

    int[] arr;

    public RadixSort(int[] arr) {
        this.arr = arr;
    }

    int[] sort() {
        if (arr.length == 1) return arr;

        int max = arr[0];
        for (int i=1; i<arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }

        int maxDigits = 0;
        while (max > 0) {
            max /= 10;
            maxDigits++;
        }

        int[] lastArr = arr;

        for (int i=0; i<maxDigits; i++) {

            int[] counts = new int[10];
            for (int j=0; j<lastArr.length; j++) {
                int digit = (lastArr[j] / RadixSort.pow(10, i)) % 10;
                counts[digit] += 1;
            }

            int nextPos = 0;
            for (int j=0; j<counts.length; j++) {
                int newPos = nextPos;
                nextPos += counts[j];
                counts[j] = newPos;
            }

            int[] startPos = counts;

            int[] res = new int[lastArr.length];

            for (int j=0; j<lastArr.length; j++) {
                int digit = (lastArr[j] / RadixSort.pow(10, i)) % 10;
                res[startPos[digit]++] = lastArr[j];
            }

            lastArr = res;
        }

        return lastArr;
    }

    static int pow(int num, int power) {
        if (power == 0) return 1;
        if (power == 1) return num;

        for (int i=2; i<=power; i++) {
            num *= 10;
        }
        return num;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{109, 2310, 5, 752, 33, 1113, 11, 997};

        RadixSort sorter = new RadixSort(arr);
        int[] res = sorter.sort();

        for (int i=0; i<res.length; i++) {
            System.out.println(res[i]);
        }
    }
}