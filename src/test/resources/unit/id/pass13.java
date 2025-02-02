class Test {

    private void Foo() {
	Other.back.mine = 3;
    }
	
    public static int mine;
}


class Other {

    public static void main(String[] args) {}

    static Test back;
}
