class TestClass {
        
    public static void StaticContext() {
        int x = TestClass.pubfield;
        int y = Other.getI(1, 2);
    }
        
    public int pubfield;
}

class Other {
    public int getI(int i, int i2) {
        return 1;
    }
}
