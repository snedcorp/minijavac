class Test {

    int getErrs(int n) {
        int err = 0;
        for (int i=0; i<n; i++) {
            if (isValid(i)) continue;
            int numErrors = getErr(i);
            err += numErrors;
        }
        return err;
    }

    boolean isValid(int i) {
        return false;
    }

    int getErr(int i) {
        return 2;
    }
}