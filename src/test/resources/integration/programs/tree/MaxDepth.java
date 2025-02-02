class MaxDepth {

    int maxDepth(TreeNode root) {
        if (root == null) return 0;
        if (root.left == null && root.right == null) return 1;

        int leftDepth = root.left != null ? maxDepth(root.left) : -1;
        int rightDepth = root.right != null ? maxDepth(root.right) : -1;

        int maxDepth = leftDepth > rightDepth ? leftDepth : rightDepth;
        return maxDepth + 1;
    }

    public static void main(String[] args) {
        MaxDepth maxDepth = new MaxDepth();

        TreeNode three = new TreeNode(3);
        TreeNode nine = new TreeNode(9);
        TreeNode twenty = new TreeNode(20);
        TreeNode fifteen = new TreeNode(15);
        TreeNode seven = new TreeNode(7);

        three.left = nine;
        three.right = twenty;
        twenty.left = fifteen;
        twenty.right = seven;

        System.out.println(maxDepth.maxDepth(three)); // 3
    }
}