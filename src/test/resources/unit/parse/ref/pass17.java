class Test {
    private void main() {
        this();
        this(1, 2);
        x(1, 2);
        this.x(1, 2);
        Test.x();
        x.p.q(1, 2, 3);
        x(1).p(2).q(3);
        this.x().p = 1;
    }
}