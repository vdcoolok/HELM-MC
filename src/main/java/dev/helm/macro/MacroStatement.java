package dev.helm.macro;

import java.util.List;

import dev.helm.input.InputBinding;
import dev.helm.rotation.Rotation;

public sealed interface MacroStatement {

    String keyword();

    record Wait(MacroDuration duration) implements MacroStatement {

        @Override
        public String keyword() {
            return "wait";
        }
    }

    record Loop(long repeats, List<MacroStatement> body) implements MacroStatement {

        public Loop {
            body = List.copyOf(body);
        }

        @Override
        public String keyword() {
            return "loop";
        }
    }

    record Move(double x, double y, double z) implements MacroStatement {

        @Override
        public String keyword() {
            return "goto";
        }
    }

    record MoveHere() implements MacroStatement {

        @Override
        public String keyword() {
            return "gotohere";
        }
    }

    record Look(Rotation rotation) implements MacroStatement {

        @Override
        public String keyword() {
            return "lookat";
        }
    }

    record LookHere() implements MacroStatement {

        @Override
        public String keyword() {
            return "lookathere";
        }
    }

    record Hold(InputBinding input) implements MacroStatement {

        @Override
        public String keyword() {
            return "hold";
        }
    }

    record Release(InputBinding input) implements MacroStatement {

        @Override
        public String keyword() {
            return "release";
        }
    }

    record Press(InputBinding input) implements MacroStatement {

        @Override
        public String keyword() {
            return "press";
        }
    }
}
