package team163.utils;

/**
 * Created by brentechols on 1/17/15.
 */
public enum CHANNELS {

    PANIC_X(911),
    PANIC_Y(912),
    SUPPLY_DRONE1(913),
    SUPPLY_DRONE2(914),
    SUPPLY_DRONE3(915),
    NUMBER_SOLDIER(1),
    NUMBER_BASHER(2),
    NUMBER_BARRACKS(3),
    NUMBER_DRONE(4),
    NUMBER_TANK(5),
    NUMBER_HELIPAD(6),
    NUMBER_AEROSPACELAB(7),
    NUMBER_BEAVER(8),
    NUMBER_COMMANDER(9),
    NUMBER_COMPUTER(10),
    NUMBER_HANDWASHSTATION(11),
    NUMBER_LAUNCHER(13),
    NUMBER_MINER(14),
    NUMBER_MINERFACTORY(15),
    NUMBER_MISSILE(16),
    NUMBER_SUPPLYDEPOT(17),
    NUMBER_TANKFACTORY(18),
    NUMBER_TECHNOLOGYINSTITUTE(19),
    NUMBER_TRAININGFIELD(21);

    private final int id;

    CHANNELS(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
