class Test {
    static int x;
    void main(int p) {
        p = ++x;
        p = --x;
        p = x++;
        p = x--;
        ++x;
        --x;
        x++;
        x--;
    }
}