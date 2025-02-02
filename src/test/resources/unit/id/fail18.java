class Test {
    public static void main(String[] args) {
        F05 c = new F05();
        c = c.foo.next;
    }
}

class F05 {
    public F05 next;
    public F05 foo() {return this;}
}
