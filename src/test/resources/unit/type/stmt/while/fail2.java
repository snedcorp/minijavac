class Test {
    public static void main(String[] args) {
        do {
            do {}
            while (new Other());
        } while (1 + 2);
    }
}

class Other {}