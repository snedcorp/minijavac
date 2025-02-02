package minijavac.gen.attribute.stackmap;

/**
 * Enumeration of the different item amounts that can get popped from the stack while executing an instruction.
 */
public enum StackPop {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    DYNAMIC(-1);

    StackPop(int amount) {
        this.amount = amount;
    }

    private final int amount;

    public int getAmount() {
        return amount;
    }
}
