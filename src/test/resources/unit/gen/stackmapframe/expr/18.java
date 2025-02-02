class Test {

    static int getI(int i) {
        return 1;
    }

    boolean getB(boolean b) {
        return true;
    }

    float getF() {
        return 1.0;
    }

    int[] getIa() {
        return new int[2];
    }

    static Test[][] getTa() {
        return new Test[2][2];
    }

    Test getT() {
        return new Test();
    }

    void main(boolean b) {
        int r = get(getI(1), getF(), getIa(), getTa(), getT(), !getB(b));
    }

    static int get(int i, float f, int[] ia, Test[][] ta, Test t, boolean b) {
        return 1;
    }
}