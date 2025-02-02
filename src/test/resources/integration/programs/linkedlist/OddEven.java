class OddEven {

    Node oddEven(Node head) {
        if (head == null) return null;

        Node odd = head;
        Node even = head.next;

        Node oddHead = odd;
        Node evenHead = even;

        if (even == null) return odd;

        while (even != null && even.next != null) {
            odd.next = even.next;
            even.next = even.next.next;
            odd = odd.next;
            even = even.next;
        }

        odd.next = evenHead;
        return oddHead;
    }

    public static void main(String[] args) {
        OddEven oddEven = new OddEven();

        Node one = new Node(1);
        Node two = new Node(2);
        Node three = new Node(3);
        Node four = new Node(4);
        Node five = new Node(5);

        one.next = two;
        two.next = three;
        three.next = four;
        four.next = five;

        Node head = oddEven.oddEven(one);

        Node curr = head;
        while (curr != null) {
            System.out.println(curr.val);
            curr = curr.next;
        }
    }
}