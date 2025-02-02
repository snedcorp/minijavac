class Test {

    public static void main(String [] args) { 

	A ai = new A();
	B bi = new B();

	ai.set();
	if (null == bi.get())
	    return;
    }
}

class A {
    B b;

    public void set() {
        b = null;
    }

}

class B {
    A a;
    
    public A get() {
        return a;
    }
}
