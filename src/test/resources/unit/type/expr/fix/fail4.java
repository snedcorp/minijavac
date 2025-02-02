class Test {
    void main() {
        ++i();
        --i();
        int p = ++i() + --i() + i()++ + i()--;
    }

    int i() { return 1; }
}