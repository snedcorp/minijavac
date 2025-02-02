class Test {
    int i;
    void main() {
        int i2 = 1;
        int res = diff(i, i2);
        int res2 = this.diff(i2, i);
    }

    int diff(int a, int b) {
        return a + b;
    }
}