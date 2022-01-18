package es.lavanda.filebot.executor.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FilebotAMCException extends RuntimeException {

    private Type type;

    private String executionMessage;

    public enum Type {
        STRICT_QUERY, REGISTER, SELECTED_OPTIONS;
    }
}
