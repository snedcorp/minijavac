class Test {
    private int i;
    private static int istat;
    private Test t;
    private static Test tstat;
    void main() {
        i = 1;
        istat = 2;
        t = tstat;
        tstat = t;
    }
}