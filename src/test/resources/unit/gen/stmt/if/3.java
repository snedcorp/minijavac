class Test {
    void main(boolean b, boolean c, boolean d) {
        if (b) get(1);
        else if (c) get(2);
        else if (d) get(3);
        else get(4);
    }

    static void get(int i) {}
}