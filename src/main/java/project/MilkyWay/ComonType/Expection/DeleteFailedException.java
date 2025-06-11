package project.MilkyWay.ComonType.Expection;

public class DeleteFailedException extends RuntimeException
{

        // 기본 생성자
        public DeleteFailedException() {
            super("삭제 실패가 발생했습니다.");
        }

        // 메시지를 받는 생성자
        public DeleteFailedException(String message) {
            super(message);
        }

        // 원인(Throwable)을 받는 생성자
        public DeleteFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        // 원인(Throwable)만 받는 생성자
        public DeleteFailedException(Throwable cause) {
            super(cause);
        }
    }

