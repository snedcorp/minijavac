class Test {

    public void f() {
        C = 5;
    }

    int C;  // hides class C at member level
    
}

class C {
    int x;
}
