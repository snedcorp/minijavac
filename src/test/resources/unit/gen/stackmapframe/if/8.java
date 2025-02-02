class Test {
    static void main(boolean b) {
        int x = 1;
        if (b) {
            int y = 2;
            if (b) {
                b = true;
            }
            y = 3;
        }
        return;
    }
}