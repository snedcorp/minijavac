class SameTree {
    boolean isSameTree(TreeNode p, TreeNode q) {
        if (p == null && q == null) return true;
        if (p == null && q != null) return false;
        if (p != null && q == null) return false;

        if (p.val != q.val) return false;
        boolean leftSame = isSameTree(p.left, q.left);
        if (!leftSame) return false;
        return isSameTree(p.right, q.right);
    }

    static void testFalse() {
        SameTree sameTree = new SameTree();

        TreeNode oneA = new TreeNode(1);
        TreeNode twoA = new TreeNode(2);
        TreeNode threeA = new TreeNode(3);

        oneA.left = twoA;
        oneA.right = threeA;

        TreeNode oneB = new TreeNode(1);
        TreeNode twoB = new TreeNode(2);
        TreeNode threeB = new TreeNode(3);

        oneB.left = twoB;
        twoB.right = threeB;

        System.out.println(sameTree.isSameTree(oneA, oneB)); // false
    }

    static void testTrue() {
        SameTree sameTree = new SameTree();

        TreeNode oneA = new TreeNode(1);
        TreeNode twoA = new TreeNode(2);
        TreeNode threeA = new TreeNode(3);

        oneA.left = twoA;
        oneA.right = threeA;

        TreeNode oneB = new TreeNode(1);
        TreeNode twoB = new TreeNode(2);
        TreeNode threeB = new TreeNode(3);

        oneB.left = twoB;
        oneB.right = threeB;

        System.out.println(sameTree.isSameTree(oneA, oneB)); // true
    }

    public static void main(String[] args) {
        testFalse();
        testTrue();
    }
}