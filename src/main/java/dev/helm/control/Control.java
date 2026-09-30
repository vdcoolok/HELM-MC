package dev.helm.control;

public enum Control {

    MOVE_FORWARD,
    MOVE_BACK,
    MOVE_LEFT,
    MOVE_RIGHT,
    JUMP,
    SNEAK,
    SPRINT,
    ATTACK,
    USE;

    public static final Control[] MOVEMENT = {
            MOVE_FORWARD, MOVE_BACK, MOVE_LEFT, MOVE_RIGHT, SNEAK, JUMP
    };
}
