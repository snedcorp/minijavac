class Test {
    public static void main(String[] args) {
        Test p = new Test();
        int x = p.p() + p.x;
    }
    
    public int x;
    
    public int p() {
        return 3;
    }
}
