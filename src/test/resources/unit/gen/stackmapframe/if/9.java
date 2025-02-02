class Test {
    static void main(boolean b) {
        int x = 1;
        if (b) {
            int y = 2;
            int z = 3;
            if (b) {
                b = true;
            }
            y = 3;
            int u = 4;
        }
        return;
    }
}