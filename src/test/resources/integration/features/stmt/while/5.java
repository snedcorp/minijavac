class Test {

    int main(boolean b, boolean c) {
        while (b) {
            if (c) break;
            return 1;
        }
        return 2;
    }

    public static void main(String[] args) {
        Test t = new Test();
        System.out.println(t.main(true, true)); // 2
        System.out.println(t.main(true, false)); // 1
        System.out.println(t.main(false, true)); // 2
        System.out.println(t.main(false, false)); // 2
    }
}