package es.lavanda.filebot.executor.exception;

import lombok.Getter;

@Getter
public class FilebotAMCException extends RuntimeException {

    private Type type;

    private String executionMessage;

    public FilebotAMCException(Type type, String executionMessage) {
        super(executionMessage);
        this.type = type;
        this.executionMessage = executionMessage;
    }

    public enum Type {
        STRICT_QUERY, REGISTER, SELECTED_OPTIONS, FILE_EXIST,FILES_NOT_FOUND;
    }
}
