package project.MilkyWay.ComonType.DTO;

import lombok.*;

import java.util.List;


@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseDTO<T>
{
    private String resultType; //어떤 유형의 데이터인지 확인
    private String message; // error message - fail
    private List<T> data; // response data - success
    private PageDTO pageDTO;


    public ResponseDTO<T> Response(String result, String message, List<T> list)
    {
        // List<T> 그대로 사용

        return ResponseDTO.<T>builder()
                .resultType(result)
                .message(message)
                .data(list) // List<T> 그대로 사용
                .build();
    }
    public ResponseDTO<T> Response(String result, String message, PageDTO list)
    {
        // List<T> 그대로 사용

        return ResponseDTO.<T>builder()
                .resultType(result)
                .message(message)
                .pageDTO(list) // List<T> 그대로 사용
                .build();
    }
    public ResponseDTO<T> Response(String result, String message)
    {
        return ResponseDTO.<T>builder()
                .resultType(result)
                .message(message)
                .build();
    }
}

// HTTP Response 할 때 사용할 DTO
// 서버에서 클라이언트로 응답할 때 사용할 데이터 구조 정의
