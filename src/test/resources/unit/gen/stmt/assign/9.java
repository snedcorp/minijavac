class Test {
    private float f;
    private static float fstat;
    void main(float p) {
        float a = 1.1;
        a = 2.2;
        a = p;
        p = a;
        f = 0.0;
        fstat = 1.0;
        f = fstat;
        fstat = f;
    }
}