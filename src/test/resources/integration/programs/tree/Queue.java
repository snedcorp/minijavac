class Queue {
    private TreeNode[] arr;
    private int head;
    private int tail;

    Queue(int maxSize) {
        arr = new TreeNode[maxSize];
        head = -1;
        tail = -1;
    }

    boolean offerLast(TreeNode node) {
        int newTail = (tail+1) % arr.length;
        if (newTail == head) return false;
        tail = newTail;
        arr[tail] = node;
        if (head == -1) head = 0;
        return true;
    }

    TreeNode peekFirst() {
        if (head == -1) return null;
        return arr[head];
    }

    TreeNode pollFirst() {
        if (head == -1) return null;
        TreeNode node = arr[head];
        if (head == tail) {
            head = -1;
            tail = -1;
        } else {
            head = (head + 1) % arr.length;
        }
        return node;
    }

    boolean isEmpty() {
        return head == -1 && tail == -1;
    }

    int size() {
        if (isEmpty()) return 0;
        return tail >= head ? (tail - head) + 1 : (tail + arr.length - head) + 1;
    }
}