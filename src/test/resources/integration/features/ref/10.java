class Test {
    public static void main(String[] args) {
        Other o = new Other(13, true);

        System.out.println(o.x);
        System.out.println(o.y);

        Other o2 = new Other(20);

        System.out.println(o2.x);
        System.out.println(o2.y);

        Other o3 = new Other(true);

        System.out.println(o3.x);
        System.out.println(o3.y);
    }
}

class Other {

    int x;
    boolean y;

    Other(int x, boolean y) {
        this.x = x;
        this.y = y;
    }

    Other(int x) {
        this(x, false);
    }

    Other(boolean y) {
        this(9, y);
    }
}