class Test {
    public static void main(String[] args) {
        int x = 0;
        while (x < 3) {
            int y = 0;
            System.out.println(x);
            while (y < 3) {
                if (y == 2) break;
                System.out.println(y);
                y++;
            }
            x++;
        }
    }
}