class Remove {

    Node remove(Node head, int n) {
        Node dummy = new Node(-1);
        dummy.next = head;
        Node curr = dummy;
        while (curr.next != null) {
            if (curr.next.val == n) {
                curr.next = curr.next.next;
            } else {
                curr = curr.next;
            }
        }
        return dummy.next;
    }

    static void print(Node head) {
        Node curr = head;
        while (curr != null) {
            System.out.println(curr.val);
            curr = curr.next;
        }
    }

    public static void main(String[] args) {
        Remove remover = new Remove();

        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        Node d = new Node(3);
        Node e = new Node(4);
        Node f = new Node(5);
        Node g = new Node(4);

        a.next = b;
        b.next = c;
        c.next = d;
        d.next = e;
        e.next = f;
        f.next = g;

        Node head = remover.remove(a, 1);
        print(head);

        head = remover.remove(head, 3);
        print(head);

        head = remover.remove(head, 4);
        print(head);
    }
}