class Test {
    public static void main(String[] args) {
        Other o = new Other();
        System.out.println(o.init(0).add(2).mult(5).res());
        System.out.println(o.init(0).mult(5).add(2).res());
        System.out.println(o.init(3).add(6).mult(2).res());
        System.out.println(o.init(3).mult(2).add(6).res());
    }
}

class Other {
    private int x;

    Other init(int i) {
        x = i;
        return this;
    }

    Other add(int s) {
        x = x + s;
        return this;
    }

    Other mult(int m) {
        x = x * m;
        return this;
    }

    int res() {
        return x;
    }
}