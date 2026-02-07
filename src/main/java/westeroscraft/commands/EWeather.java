package westeroscraft.commands;

public enum EWeather {
    CLEAR("clear"),
    RAIN("rain"),
    RESET("reset"),
    THUNDER("thunder");

    public final String string;

    EWeather(String w) { this.string = w; }
}
