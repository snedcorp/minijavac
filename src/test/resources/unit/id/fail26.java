class TestClass {
        
    public void nonStaticContext() {

        int x = OtherClass.opubstatTest.privfn;
    }
        
        
    private int privfn() { return 1; }
}

class OtherClass {
        
    public static TestClass opubstatTest;
}
