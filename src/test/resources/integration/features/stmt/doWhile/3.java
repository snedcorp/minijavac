class Test {
    public static void main(String[] args) {
        int x = 0;
        do {
            x++;
            if (x % 2 == 0) {
                continue;
            }
            System.out.println(x);
        } while (x < 10);
    }
}