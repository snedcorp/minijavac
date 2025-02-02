class ValidateBST {

    int prev;

    ValidateBST() {
        prev = -1;
    }

    public boolean isValidBST(TreeNode root) {
        if (root == null) return true;

        boolean leftValid = isValidBST(root.left);
        if (!leftValid || prev > root.val) return false;

        prev = root.val;

        return isValidBST(root.right);
    }

    private static void testTrue() {
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);
        TreeNode three = new TreeNode(3);

        two.left = one;
        two.right = three;

        ValidateBST validator = new ValidateBST();
        System.out.println(validator.isValidBST(two)); // true
    }

    private static void testFalse() {
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);
        TreeNode three = new TreeNode(3);

        one.left = two;
        one.right = three;

        ValidateBST validator = new ValidateBST();
        System.out.println(validator.isValidBST(one)); // false
    }

    public static void main(String[] args) {
        testTrue();
        testFalse();
    }
}