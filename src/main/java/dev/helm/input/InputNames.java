package dev.helm.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InputNames {

    private static final List<String> MOUSE = List.of("M1", "M2", "M3");
    private static final List<String> SCROLL = List.of("SCROLLUP", "SCROLLDOWN");

    private InputNames() {
    }

    public static List<InputBinding> all() {
        List<InputBinding> bindings = new ArrayList<>();
        for (String name : KeyCodes.listed()) {
            Integer code = KeyCodes.find(name);
            if (code != null) {
                bindings.add(new InputBinding(InputKind.KEYBOARD, code, name));
            }
        }
        for (String name : MOUSE) {
            bindings.add(find(name).orElseThrow());
        }
        for (String name : SCROLL) {
            bindings.add(find(name).orElseThrow());
        }
        return bindings;
    }

    public static String describe(InputBinding binding) {
        return switch (binding.kind()) {
            case MOUSE -> switch (binding.label()) {
                case "M1" -> "left mouse button";
                case "M2" -> "right mouse button";
                case "M3" -> "middle mouse button";
                default -> "mouse button";
            };
            case SCROLL -> binding.label().equals("SCROLLUP")
                    ? "scroll wheel up" : "scroll wheel down";
            case KEYBOARD -> KeyCodes.describe(binding.label());
        };
    }

    public static Optional<InputBinding> find(String token) {
        String name = KeyCodes.normalize(token);
        if (name.isEmpty()) {
            return Optional.empty();
        }

        Integer keyboard = KeyCodes.find(name);
        if (keyboard != null) {
            return Optional.of(new InputBinding(InputKind.KEYBOARD, keyboard, name));
        }

        return switch (name) {
            case "M1", "LEFT", "BUTTON1" -> mouse(0, "M1");
            case "M2", "RIGHT", "BUTTON2" -> mouse(1, "M2");
            case "M3", "MIDDLE", "BUTTON3" -> mouse(2, "M3");
            case "SCROLLUP", "WHEELUP", "SCROLL_UP" -> scroll(0, "SCROLLUP");
            case "SCROLLDOWN", "WHEELDOWN", "SCROLL_DOWN" -> scroll(1, "SCROLLDOWN");
            default -> Optional.empty();
        };
    }

    public static InputBinding require(String token) {
        return find(token).orElseThrow(() -> new IllegalArgumentException("Unknown input: " + token));
    }

    private static Optional<InputBinding> mouse(int code, String label) {
        return Optional.of(new InputBinding(InputKind.MOUSE, code, label));
    }

    private static Optional<InputBinding> scroll(int code, String label) {
        return Optional.of(new InputBinding(InputKind.SCROLL, code, label));
    }
}
