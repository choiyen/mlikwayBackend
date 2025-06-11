package project.MilkyWay.ComonType.Enum;

public enum ErrorCode {
    EMPTY_FILE_EXCEPTION("Empty file exception"),
    NO_FILE_EXTENTION("No file extension found"),
    INVALID_FILE_EXTENTION("Invalid file extension"),
    IO_EXCEPTION_ON_IMAGE_UPLOAD("I/O exception occurred while uploading the image"),
    PUT_OBJECT_EXCEPTION("Error occurred while putting object to S3"),
    IO_EXCEPTION_ON_IMAGE_DELETE("I/O exception occurred while deleting the image from S3");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

