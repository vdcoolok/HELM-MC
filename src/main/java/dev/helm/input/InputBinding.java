package dev.helm.input;

public record InputBinding(InputKind kind, int code, String label) {

    public InputBinding {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("An input needs a label");
        }
    }

    public boolean isKeyboard() {
        return kind == InputKind.KEYBOARD;
    }

    public boolean isMouse() {
        return kind == InputKind.MOUSE;
    }

    public boolean isScroll() {
        return kind == InputKind.SCROLL;
    }

    @Override
    public String toString() {
        return label;
    }
}
