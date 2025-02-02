class Test {
    public static void main(String[] args) {
        for (int i=0; i<3; i++) {
            System.out.println(i);
            for (int j=0; j<3; j++) {
                if (j == 2) break;
                System.out.println(j);
            }
        }
    }
}