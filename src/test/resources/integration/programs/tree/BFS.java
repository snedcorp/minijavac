class BFS {

    void search(TreeNode head) {
        if (head == null) return;

        Queue queue = new Queue(10);

        queue.offerLast(head);

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i=0; i<size; i++) {
                TreeNode node = queue.pollFirst();
                System.out.println(node.val);
                if (node.left != null) {
                    queue.offerLast(node.left);
                }
                if (node.right != null) {
                    queue.offerLast(node.right);
                }
            }
        }
    }

    private static void testOne() {
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);
        TreeNode three = new TreeNode(3);

        one.left = two;
        one.right = three;

        BFS bfs = new BFS();
        bfs.search(one);
    }

    private static void testTwo() {
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);
        TreeNode three = new TreeNode(3);
        TreeNode four = new TreeNode(4);
        TreeNode five = new TreeNode(5);
        TreeNode six = new TreeNode(6);
        TreeNode seven = new TreeNode(7);

        one.left = two;
        one.right = three;
        two.left = four;
        two.right = five;
        three.left = six;
        three.right = seven;

        BFS bfs = new BFS();
        bfs.search(one);
    }

    public static void main(String[] args) {
        testOne();
    }
}