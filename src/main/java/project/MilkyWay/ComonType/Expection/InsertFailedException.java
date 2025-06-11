package project.MilkyWay.ComonType.Expection;

public class InsertFailedException extends RuntimeException
{
    // 기본 생성자
    public InsertFailedException() {
        super("데이터 추가에 실패가 발생했습니다.");
    }

    // 메시지를 받는 생성자
    public InsertFailedException(String message) {
        super(message);
    }

    // 원인(Throwable)을 받는 생성자
    public InsertFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    // 원인(Throwable)만 받는 생성자
    public InsertFailedException(Throwable cause) {
        super(cause);
    }
}
