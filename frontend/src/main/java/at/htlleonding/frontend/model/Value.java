package at.htlleonding.frontend.model;

public enum Value {
    A(0),
    _2(2),
    _3(3),
    _4(4),
    _5(5),
    _6(6),
    _7(7),
    _8(8),
    _9(9),
    _10(10),
    J(10),
    Q(10),
    K(10);

    Value(int value) {
        this.value = value;
    }

    private final int value;

    public int getValue() {
        return value;
    }
}
