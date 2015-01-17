package team163.utils;

/**
 * Created by brentechols on 1/17/15.
 */
public enum CHANNELS {
    PANIC_X(911),
    PANIC_Y(912)
    ;

    private final int id;
    CHANNELS(int id) { this.id = id; }
    public int getValue() { return id; }
}
