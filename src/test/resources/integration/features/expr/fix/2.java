class Test {
    public static void main(String[] args) {
        float i = 0.5;
        float p = ++i;
        System.out.println(i);
        System.out.println(p);
        float r = i++;
        System.out.println(i);
        System.out.println(r);
    }
}