package exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends CinemaException {
    private List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    @Override
    public String getMessage() {
        if (hasErrors()) {
            return String.join("\n", errors);
        }
        return super.getMessage();
    }
}