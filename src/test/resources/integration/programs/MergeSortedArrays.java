class MergeSortedArrays {

    int[] merge(int[] a, int[] b) {
        int[] res = new int[a.length + b.length];
        int aIx = 0;
        int bIx = 0;
        while (aIx < a.length || bIx < b.length) {
            int aVal = aIx < a.length ? a[aIx] : 100;
            int bVal = bIx < b.length ? b[bIx] : 100;

            if (aVal < bVal) {
                res[aIx + bIx] = aVal;
                aIx++;
            } else {
                res[aIx + bIx] = bVal;
                bIx++;
            }
        }

        return res;
    }

    public static void main(String[] args) {
        MergeSortedArrays merger = new MergeSortedArrays();

        int[] a = new int[]{1, 3, 3, 5, 6, 7, 10};
        int[] b = new int[]{0, 1, 2, 3, 8, 9, 11};

        int[] res = merger.merge(a, b);
        for (int i=0; i<res.length; i++) {
            System.out.println(res[i]);
        }
    }
}