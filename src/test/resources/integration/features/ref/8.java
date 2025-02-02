class Test {
    public static void main(String[] args) {
        Other o = new Other();
        System.out.println(o.init(0.0).add(2.0).mult(5.0).res());
        System.out.println(o.init(0.0).mult(5.0).add(2.0).res());
        System.out.println(o.init(3.0).add(6.0).mult(2.0).res());
        System.out.println(o.init(3.0).mult(2.0).add(6.0).res());
    }
}

class Other {
    private float x;

    Other init(float i) {
        x = i;
        return this;
    }

    Other add(float s) {
        x = x + s;
        return this;
    }

    Other mult(float m) {
        x = x * m;
        return this;
    }

    float res() {
        return x;
    }
}