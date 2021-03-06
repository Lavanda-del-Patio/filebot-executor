package es.lavanda.filebot.executor.util;

public enum FilebotConstants {

    SCRIPT_AMC(" -script 'fn:amc' "),
    NO_XATTR(" -no-xattr "),
    NON_STRICT(" -non-strict "),
    ACTION_TEST(" --action test "),
    ACTION_MOVE(" --action copy "),
    LANG_ES(" --lang es "),
    LANG_EN(" --lang en "),
    ORDER_AIRDATE(" --order Airdate "),
    DEF(" --def "),
    LICENSE(" --license ");

    private final String text;

    FilebotConstants(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
