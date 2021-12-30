package es.lavanda.filebot.executor.util;

public enum FilebotConstants {

    SCRIPT_AMC(" -script 'fn:amc' "),
    NO_XATTR(" -no-xattr "),
    NON_STRICT(" -non-strict "),
    ACTION_TEST(" --action test "),
    ACTION_MOVE(" --action move "),
    LANG_ES(" --lang es "),
    ORDER_AIRDATE(" --order Airdate "),
    DEF(" --def ");

    private final String text;

    FilebotConstants(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
