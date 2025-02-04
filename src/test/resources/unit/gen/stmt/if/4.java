class Test {

    int getI(boolean b, boolean c) {
        if (b) return 1;
        else if (c) return 2;
        return 3;
    }

    float getF(boolean b, boolean c) {
        if (b) return 1.1;
        else if (c) return 2.2;
        return 3.3;
    }

    Test getTest(boolean b, boolean c, Test t1, Test t2, Test t3) {
        if (b) return t1;
        else if (c) return t2;
        return t3;
    }

    void getV(boolean b, boolean c) {
        if (b) return;
        else if (c) return;
        return;
    }
}