class Test {

    int status;

    public static void main(String[] args) {
        Test t = new Test();
        Test res1 = foo(t);
        System.out.println(res1.status);
        Test res2 = foo(null);
        System.out.println(res2.status);
    }

    private static Test foo(Test t) {
        if (t == null) {
            t = new Test();
            t.status = 2;
        } else {
            t.status = 1;
        }
        return t;
    }
}