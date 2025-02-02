class Test {
    float[] fix;
    void main(float p) {
        float[] fx = new float[2];
        float[][] fx2 = new float[2][2];
        p = ++fx[1];
        p = ++fx2[1][2];
        p = ++fix[1];
        p = --fx[1];
        p = fx[1]++;
        p = fx[1]--;
        ++fx[1];
        --fx[1];
        fx[1]++;
        fx[1]--;
        Other.fix[1]--;
    }
}

class Other {
    static float[] fix;
}