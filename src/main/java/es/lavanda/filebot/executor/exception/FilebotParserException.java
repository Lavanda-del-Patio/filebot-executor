package es.lavanda.filebot.executor.exception;

public class FilebotParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FilebotParserException(String message, Exception e) {
        super(message, e);
    }

    public FilebotParserException(String message) {
        super(message);
    }

    public FilebotParserException(Exception e) {
        super(e);
    }

}
