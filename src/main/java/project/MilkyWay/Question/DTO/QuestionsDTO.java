package project.MilkyWay.Question.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class QuestionsDTO //고객 질문을 관리하기 위한 DTO
{


    @Schema(description = "질문 Id", example = "dfsafdasf")
    @JsonProperty("id")
    private Long id; // Q&A 질문을 등록하기 위한 것

    @NotBlank(message = "ExceptionQA cannot be empty")
    @Size(min = 5, max= 100, message = "예상 Q&A의 질문은 최소 다섯자리 이상에 50자리 이하로 입력해야 함.")
    @Schema(description = "예상 질문", example = "새집증후군 신청 시 추가 요금은 대략 어떻게 정해지나요?")
    @JsonProperty("exceptionQA")
    private  String exceptionQA; // 예상했던 Q&A 질문

    @NotBlank(message = "ExceptedComment cannot be empty")
    @Size(min = 5, message = "예상 질문 코멘트는 최소 다섯자리 이상 입력해야 함.")
    @Schema(description = "예상 질문에 대한 답변", example = "평수에 따라 다르게 책정됩니다.")
    @JsonProperty("expectedComment")
    private String expectedComment; // 예상질문에 대한 해답
}
