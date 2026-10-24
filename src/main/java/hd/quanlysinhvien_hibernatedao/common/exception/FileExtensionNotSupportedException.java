
package hd.quanlysinhvien_hibernatedao.common.exception;

public class FileExtensionNotSupportedException extends RuntimeException {

    public FileExtensionNotSupportedException(String message) {
        super(message);
    }

    public FileExtensionNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
