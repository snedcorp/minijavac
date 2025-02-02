class Test {
    public static void main(String[] args) {
        Test p = new Test();
        p.next = p;
        p.next.next.x = 3;
    } 
    
    public Test next;
    private int x;
}
