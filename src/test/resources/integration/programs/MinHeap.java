class MinHeap {

    int[] arr;
    int size;

    MinHeap(int size) {
        arr = new int[size+1];
        size = 0;
    }

    boolean offer(int val) {
        if (size == arr.length-1) return false;

        arr[++size] = val;
        int i = size;

        while (i > 0) {
            int parentIx = i/2;
            if (arr[i] >= arr[parentIx]) break;
            int tmp = arr[i];
            arr[i] = arr[parentIx];
            arr[parentIx] = tmp;
            i = parentIx;
        }

        return true;
    }

    int peek() {
        if (size == 0) return -1;
        return arr[1];
    }

    int poll() {
        if (size == 0) return -1;
        int res = arr[1];

        arr[1] = arr[size];
        arr[size--] = 0;

        int i = 1;
        while (i <= size/2) {
            int leftIx = i*2;
            int rightIx = (i*2) + 1;

            int childIx = rightIx > size ? leftIx : arr[leftIx] <= arr[rightIx] ? leftIx : rightIx;
            if (arr[childIx] >= arr[i]) break;

            int tmp = arr[i];
            arr[i] = arr[childIx];
            arr[childIx] = tmp;
            i = childIx;
        }

        return res;
    }

    int size() {
        return size;
    }

    public static void main(String[] args) {
        MinHeap heap = new MinHeap(5);
        System.out.println(heap.size()); // 0
        System.out.println(heap.offer(3)); // true
        System.out.println(heap.size); // 1
        System.out.println(heap.peek()); // 3
        System.out.println(heap.offer(5)); // true
        System.out.println(heap.peek()); // 3
        System.out.println(heap.offer(2)); // true
        System.out.println(heap.peek()); // 2
        System.out.println(heap.offer(4)); // true
        System.out.println(heap.peek()); // 2
        System.out.println(heap.offer(1)); // true
        System.out.println(heap.peek()); // 1
        System.out.println(heap.size()); // 5
        System.out.println(heap.offer(6)); // false

        for (int i=1; i<6; i++) {
            System.out.println(heap.arr[i]); // [1 2 3 5 4]
        }

        System.out.println(heap.poll()); // 1
        System.out.println(heap.size()); // 4

        for (int i=1; i<5; i++) {
            System.out.println(heap.arr[i]); // [2, 4, 3, 5]
        }

        System.out.println(heap.poll()); // 2

        for (int i=1; i<4; i++) {
            System.out.println(heap.arr[i]); // [3, 4, 5]
        }

        System.out.println(heap.poll()); // 3

        for (int i=1; i<3; i++) {
            System.out.println(heap.arr[i]); // [4, 5]
        }

        System.out.println(heap.poll()); // 4

        for (int i=1; i<2; i++) {
            System.out.println(heap.arr[i]); // [5]
        }

        System.out.println(heap.poll()); // 5
        System.out.println(heap.size()); // 0
    }
}