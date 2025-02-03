class Reverse {

    Node reverse(Node head) {
        if (head.next == null) return head;
        Node toRight = reverse(head.next);
        head.next.next = head;
        head.next = null;
        return toRight;
    }

    public static void main(String[] args) {
        Reverse reverser = new Reverse();

        Node one = new Node(1);
        Node two = new Node(2);
        Node three = new Node(3);
        one.next = two;
        two.next = three;

        Node revHead = reverser.reverse(one);
        Node curr = revHead;
        while (curr != null) {
            System.out.println(curr.val);
            curr = curr.next;
        }
    }
}