class Queue {

    private int[] arr;
    private int head;
    private int tail;

    Queue(int maxSize) {
        arr = new int[maxSize];
        head = -1;
        tail = -1;
    }

    boolean offerLast(int val) {
        int newTail = (tail+1) % arr.length;
        if (newTail == head) return false;
        tail = newTail;
        arr[tail] = val;
        if (head == -1) head = 0;
        return true;
    }

    int peekFirst() {
        if (head == -1) return -1;
        return arr[head];
    }

    int pollFirst() {
        if (head == -1) return -1;
        int res = arr[head];
        if (head == tail) {
            head = -1;
            tail = -1;
        } else {
            head = (head + 1) % arr.length;
        }
        return res;
    }

    boolean isEmpty() {
        return head == -1 && tail == -1;
    }

    int size() {
        if (isEmpty()) return 0;
        return tail >= head ? (tail - head) + 1 : (tail + arr.length - head) + 1;
    }

    public static void main(String[] args) {
        Queue queue = new Queue(5);

        System.out.println(queue.peekFirst()); // -1
        System.out.println(queue.pollFirst()); // -1
        System.out.println(queue.isEmpty()); // true
        System.out.println(queue.size()); // 0
        System.out.println(queue.offerLast(1)); // true
        System.out.println(queue.pollFirst()); // 1
        System.out.println(queue.peekFirst()); // -1
        System.out.println(queue.offerLast(2)); // true
        System.out.println(queue.offerLast(3)); // true
        System.out.println(queue.offerLast(4)); // true
        System.out.println(queue.offerLast(5)); // true
        System.out.println(queue.offerLast(6)); // true
        System.out.println(queue.size()); // 5
        System.out.println(queue.isEmpty()); // false
        System.out.println(queue.offerLast(7)); // false
        System.out.println(queue.peekFirst()); // 2
        System.out.println(queue.pollFirst()); // 2
        System.out.println(queue.offerLast(8)); // true
        System.out.println(queue.size()); // 5
        System.out.println(queue.pollFirst()); // 3
        System.out.println(queue.pollFirst()); // 4
        System.out.println(queue.pollFirst()); // 5
        System.out.println(queue.pollFirst()); // 6
        System.out.println(queue.size()); // 1
        System.out.println(queue.pollFirst()); // 8
        System.out.println(queue.isEmpty()); // true
        System.out.println(queue.peekFirst()); // -1
        System.out.println(queue.size()); // 0
    }
}