class InOrder {

    void traverse(TreeNode node) {
        if (node == null) return;
        traverse(node.left);
        System.out.println(node.val);
        traverse(node.right);
    }

    public static void main(String[] args) {
        InOrder inOrder = new InOrder();

        TreeNode n3 = new TreeNode(3);
        TreeNode n1 = new TreeNode(1);
        n3.left = n1;
        TreeNode n0 = new TreeNode(0);
        TreeNode n2 = new TreeNode(2);
        n1.left = n0;
        n1.right = n2;
        TreeNode n5 = new TreeNode(5);
        TreeNode n4 = new TreeNode(4);
        TreeNode n6 = new TreeNode(6);
        n3.right = n5;
        n5.left = n4;
        n5.right = n6;

        inOrder.traverse(n3);
    }
}