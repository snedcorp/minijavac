class Test {
    int x;
    public static void main(String[] args) {
        System.out.println(true ? 1 : 2);
        System.out.println(false ? 1 : 2);
        System.out.println(true ? getT(1).x : getT(2).x);
        System.out.println(false ? getT(1).x : getT(2).x);
    }

    static Test getT(int i) {
        Test t = new Test();
        t.x = i;
        return t;
    }
}