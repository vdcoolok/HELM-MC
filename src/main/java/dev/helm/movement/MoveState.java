package dev.helm.movement;

public enum MoveState {

    PREPPING,
    WAITING,
    RUNNING,
    SUCCESS,
    UNREACHABLE,
    FAILED;

    public boolean complete() {
        return this == SUCCESS || this == UNREACHABLE || this == FAILED;
    }
}
