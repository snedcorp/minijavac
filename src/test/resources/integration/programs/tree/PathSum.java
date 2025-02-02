class PathSum {

    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;
        return hasSum(root, targetSum);
    }

    private boolean hasSum(TreeNode root, int targetSum) {
        if (root == null) return targetSum == 0;

        int newVal = targetSum - root.val;
        if (root.left == null && root.right == null) return newVal == 0;

        boolean leftPath = hasPathSum(root.left, newVal);
        if (leftPath) return true;
        return hasPathSum(root.right, newVal);
    }

    private static void testFalse() {
        TreeNode five = new TreeNode(5);
        TreeNode three = new TreeNode(3);
        TreeNode six = new TreeNode(6);
        TreeNode two = new TreeNode(2);
        TreeNode seven = new TreeNode(7);

        five.left = three;
        five.right = six;
        three.left = two;
        three.right = seven;

        PathSum pathSum = new PathSum();
        System.out.println(pathSum.hasPathSum(five, 9)); // false
    }

    private static void testTrue() {
        TreeNode five = new TreeNode(5);
        TreeNode three = new TreeNode(3);
        TreeNode six = new TreeNode(6);
        TreeNode two = new TreeNode(2);
        TreeNode seven = new TreeNode(7);

        five.left = three;
        five.right = six;
        three.left = two;
        three.right = seven;

        PathSum pathSum = new PathSum();
        System.out.println(pathSum.hasPathSum(five, 15)); // true
    }

    public static void main(String[] args) {
        testFalse();
        testTrue();
    }
}