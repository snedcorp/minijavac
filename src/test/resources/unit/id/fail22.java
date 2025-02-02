class TestClass {
        
    public static void staticContext() {
        int x = Other.privstatfield;
        int y = Other.privstatmethod();
    }
}        

class Other {
    private static int privstatfield;
    private static int privstatmethod() {
        return 1;
    }
}
