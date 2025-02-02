class LinkedList {

    Node head;

    public int get(int index) {
        Node node = traverse(index);
        if (node == null) return -1;
        return node.val;
    }

    private Node traverse(int index) {
        Node curr = head;
        int i = 0;
        while (i < index) {
            if (curr == null || curr.next == null) return null;
            curr = curr.next;
            i++;
        }
        return curr;
    }

    public void addAtHead(int val) {
        Node node = new Node(val);
        node.next = head;
        head = node;
    }

    public void addAtTail(int val) {
        Node node = new Node(val);
        if (head == null) {
            head = node;
            return;
        }

        Node curr = head;
        while (curr.next != null) {
            curr = curr.next;
        }
        curr.next = node;
    }

    public void addAtIndex(int index, int val) {
        if (index == 0) {
            addAtHead(val);
            return;
        }

        Node prevNode = traverse(index-1);
        if (prevNode == null) return;

        Node node = new Node(val);
        node.next = prevNode.next;
        prevNode.next = node;
    }

    public void deleteAtIndex(int index) {
        if (index == 0) {
            if (head != null) head = head.next;
            return;
        }

        Node prevNode = traverse(index-1);
        if (prevNode == null || prevNode.next == null) return;

        prevNode.next = prevNode.next.next;
    }

    public static void main(String[] args) {
        LinkedList list1 = new LinkedList();
        list1.addAtHead(1);
        list1.addAtTail(3);
        list1.addAtIndex(1, 2);
        System.out.println(list1.get(1)); // 2
        list1.deleteAtIndex(1);
        System.out.println(list1.get(1)); // 3

        LinkedList list2 = new LinkedList();
        list2.deleteAtIndex(0);

        LinkedList list3 = new LinkedList();
        list3.addAtHead(2);
        list3.deleteAtIndex(1);
        list3.addAtHead(2);
        list3.addAtHead(7);
        list3.addAtHead(3);
        list3.addAtHead(2);
        list3.addAtHead(5);
        list3.addAtTail(5);
        System.out.println(list3.get(5)); // 2
        list3.deleteAtIndex(6);
        list3.deleteAtIndex(4);

        Node curr = list3.head;
        while (curr != null) {
            System.out.println(curr.val);
            curr = curr.next;
        }
    }
}