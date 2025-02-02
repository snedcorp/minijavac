class Test {
    static float x;
    void main(float p) {
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