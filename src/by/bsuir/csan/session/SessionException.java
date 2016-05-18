package by.bsuir.csan.session;

public class SessionException extends Exception {

    private String cause;

    public SessionException() {}

    public SessionException(String cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {

        if (cause != null)
            return cause;

        return super.getMessage();
    }
}
