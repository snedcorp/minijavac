class Test {
    int a;
    void main() {
        --a;
        ++a;
        a--;
        a++;
        int x = 1 + --a;
        x = 1 - ++a;
        x = 1 * a--;
        x = 1 / a++;
    }
}