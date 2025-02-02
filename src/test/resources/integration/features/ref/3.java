class Test {
    int p;
    public static void main(String[] args) {
        System.out.println(get()[0]);
        System.out.println(getT()[1].p);
    }

    static int[] get() {
        int[] arr = new int[2];
        arr[0] = 13;
        return arr;
    }

    static Test[] getT() {
        Test[] arr = new Test[2];
        arr[1] = new Test();
        arr[1].p = 19;
        return arr;
    }
}