class LowestCommonAncestor {

    TreeNode lca;
    int p;
    int q;

    TreeNode search(TreeNode root, int p, int q) {
        this.p = p;
        this.q = q;
        search(root);
        return lca;
    }

    private int search(TreeNode root) {
        if (root == null) return 0;

        int resLeft = search(root.left);
        if (resLeft == 3) return resLeft;

        int resRight = search(root.right);
        if (resRight == 3) return resRight;

        int res = resLeft + resRight;
        if (res < 3) {
            if (root.val == p) {
                res += 1;
            } else if (root.val == q) {
                res += 2;
            }
        }

        if (res == 3) {
            lca = root;
        }

        return res;
    }

    private static void testOne() {
        TreeNode zero = new TreeNode(0);
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);
        TreeNode three = new TreeNode(3);
        TreeNode four = new TreeNode(4);
        TreeNode five = new TreeNode(5);
        TreeNode six = new TreeNode(6);
        TreeNode seven = new TreeNode(7);
        TreeNode eight = new TreeNode(8);

        three.left = five;
        three.right = one;
        five.left = six;
        five.right = two;
        two.left = seven;
        two.right = four;
        one.left = zero;
        one.right = eight;

        LowestCommonAncestor lca = new LowestCommonAncestor();
        System.out.println(lca.search(three, 5, 1).val); // 3
        System.out.println(lca.search(three, 5, 4).val); // 5
    }

    private static void testTwo() {
        TreeNode one = new TreeNode(1);
        TreeNode two = new TreeNode(2);

        one.left = two;
        LowestCommonAncestor lca = new LowestCommonAncestor();
        System.out.println(lca.search(one, 1, 2).val); // 1
    }

    public static void main(String[] args) {
        testOne();
        testTwo();
    }
}