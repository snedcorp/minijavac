class Test {

    int[] getNthRow(int n) {
        int[] prevRow = new int[]{1};

        for (int i=2; i<=n; i++) {
            int[] newRow = new int[i];
            newRow[0] = 1;
            for (int j=1; j<i-1; j++) {
                newRow[j] = prevRow[j-1] + prevRow[j];
            }
            newRow[i-1] = 1;
            prevRow = newRow;
        }

        return prevRow;
    }
}