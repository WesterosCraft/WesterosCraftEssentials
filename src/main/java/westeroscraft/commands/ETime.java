package westeroscraft.commands;

public enum ETime {
    SUNRISE(0),
    DAY(1000),
    MORNING(2000),
    NOON(6000),
    AFTERNOON(9000),
    SUNSET(12000),
    NIGHT(13000),
    MIDNIGHT(18000);

    public final int time;

    ETime(int i) {
        this.time = i;
    }
}
