package project.MilkyWay.ComonType.Expection;

public class FindFailedException extends RuntimeException
{
    // 기본 생성자
    public FindFailedException() {
        super("데이터 찾기에 실패했습니다.");
    }

    // 메시지를 받는 생성자
    public FindFailedException(String message) {
        super(message);
    }

    // 원인(Throwable)을 받는 생성자
    public FindFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    // 원인(Throwable)만 받는 생성자
    public FindFailedException(Throwable cause) {
        super(cause);
    }
}
