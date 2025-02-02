class Test {
    static void main() {
        int i = 0;
        int d = 0;
        do {
            i++;
            if (i < 3) continue;
            d++;
        } while (i < 10);
    }
}