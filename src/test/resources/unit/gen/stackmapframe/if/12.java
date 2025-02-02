class Test {
    static void main(boolean b) {
        int x = 1;
        if (b) {
            int y = 2;
            if (b) {
                b = true;
            }
            int z = 3;
            int zz = 4;
            int zzz = 5;
            int zzzz = 5;

            if (b) {
                b = true;
            }
            int zzzzz = 5;
            if (b) {
                b = true;
            }
            int f = 1;
            int g = 1;
            if (b) {
                b = true;
            }
            b = false;
        }
        return;
    }
}