class Test {
    public static void main(String[] args) {
        int i = 0;
        int p = ++i;
        System.out.println(i);
        System.out.println(p);
        int r = i++;
        System.out.println(i);
        System.out.println(r);
    }
}