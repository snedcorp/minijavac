class Test {
    private void main() {
        r = this.x();
        r = x.p.q();
        r = x.p[1].q();
        r = x().p;
        r = x(1).p(1, 2).q;
        r = x(1).p(1, 2).q(1, 2, 3);
    }
}