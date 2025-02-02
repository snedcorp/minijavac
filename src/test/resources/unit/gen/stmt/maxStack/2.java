class Test {
    void a() {
        i3(1, 2, 3); // 4
    }

    void b() {
        i3(1, 2, i3(1, 2, 3)); // 7
    }

    void c() {
        int x = 1;
        x = x + i3(1, 2, i3(1, 2, 3)); // 8
    }

    void d() {
        int x = i2(1, i3(1, 2, 3)); // 6
    }

    int i3(int a, int b, int c) {
        return a + b + c;
    }

    int i2(int a, int b) {
        return a + b;
    }
}