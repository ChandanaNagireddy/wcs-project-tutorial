
package uk.co.manifesto.wcs.mvc.injection;

public class InjectionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -359592190848608828L;

    public InjectionException() {
        super();
    }

    public InjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InjectionException(String message) {
        super(message);
    }

    public InjectionException(Throwable cause) {
        super(cause);
    }

}
