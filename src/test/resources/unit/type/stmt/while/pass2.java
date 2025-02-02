class Test {
    public static void main(String[] args) {
        int x = 0;
        do {
            x++;
        }
        while (1 < 2);

        do {
            x++;
        }
        while (true || false);
    }
}