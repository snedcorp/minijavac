class Test {
    void main(boolean b, boolean c) {
        while (b) {
            continue;
            while (c) {
                break;
            }
            break;
        }
        return;
    }
}