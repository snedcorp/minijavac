class Test {
    static void main(boolean b) {
        int x = 1;
        if (b) {
            int y = 2;
            if (b) {
                b = false;
            }
            b = true;
        } else {
            int y = 3;
            if (b) {
                b = false;
            }
        }
        return;
    }
}