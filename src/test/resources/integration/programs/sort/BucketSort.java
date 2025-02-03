class BucketSort {

    int[] arr;
    int numBuckets;
    Node[] buckets;

    BucketSort(int[] arr, int numBuckets) {
        this.arr = arr;
        this.numBuckets = numBuckets;
        buckets = new Node[numBuckets];
    }

    int[] sort() {
        int max = arr[0];
        for (int i=1; i<arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }

        for (int i=0; i<arr.length; i++) {
            int bucketIx = (arr[i] * numBuckets) / max;
            if (bucketIx == numBuckets) bucketIx--;
            addToBucket(bucketIx, arr[i]);
        }

        int[] res = new int[arr.length];
        int added = 0;

        for (int i=0; i<numBuckets; i++) {
            insertionSort(i);
            Node node = buckets[i];
            while (node != null) {
                res[added++] = node.val;
                node = node.next;
            }
        }

        return res;
    }

    void addToBucket(int ix, int val) {
        Node head = buckets[ix];
        Node node = new Node(val);

        if (head != null) {
            node.next = head;
            head.prev = node;
        }

        buckets[ix] = node;
    }

    void insertionSort(int ix) {
        Node head = buckets[ix];
        if (head == null) return;

        Node node = head.next;
        if (node == null) return;

        int val = node.val;
        Node comp = node.prev;
        while (comp != null && comp.val > val) {
            comp.next.val = comp.val;
            comp = comp.prev;
        }
        if (comp != null) {
            comp.next.val = val;
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{42, 84, 30, 61, 22, 25, 56, 91, 74, 11, 100, 89, 77, 3, 13, 49, 65};

        BubbleSort sorter = new BubbleSort(arr);
        sorter.sort();

        for (int i=0; i<arr.length; i++) {
            System.out.println(arr[i]);
        }
    }
}

class Node {
    int val;
    Node prev;
    Node next;

    Node(int val) {
        this.val = val;
    }
}