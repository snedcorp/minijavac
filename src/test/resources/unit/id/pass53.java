class Test {
    void main() {
        while (true) {
            continue;
            while (true) {
                continue;
                break;
            }
            break;
        }

        for (int i=0; i<10; i++) {
            continue;
            for (int j=0; j<10; j++) {
                continue;
                break;
            }
            break;
        }
    }
}