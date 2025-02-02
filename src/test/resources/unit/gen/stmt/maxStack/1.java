class Test {
    int[] ix;
    static void a() {
        int x = 1 + 2; // 2
    }

    static void b() {
        int y = 1 + 2 - 3 + 4; // 2
    }

    void c() {
        int a = ix[1]; // 2
    }

    void d() {
        int a = ix[1]; // 2
        ix[1] = 2 + 3; // 4
    }

    void e() {
        /**
         * aload_0 (1)
         * getfield (1)
         * aload_0 (2)
         * getfield (2)
         * aload_0 (3)
         * getfield (3)
         * iconst_1 (4)
         * iconst_2 (5)
         * iadd (4)
         * iaload (3)
         * iaload (2)
         * iaload (1)
         * istore_0 (0)
         * */
        int x = ix[ix[ix[1+2]]]; // 5
        int y = 1 + 2; // 2
    }
}