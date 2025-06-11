package project.MilkyWay.noticeMain.Notice.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.EnumValue;

@Getter
@Builder
@Jacksonized  // 여기를 추가하세요
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class NoticeDTO
{
    @Schema(description = "후기에 대한 Id", example = "5555")
    private String noticeId; // 후기 ID : primary key이자 10자리의 렌덤키

    @Schema(description = "어떤 유형의 일?", example = "이사청소")
    @EnumValue(enumClass = CleanType.class, message = "청소 리스트에 포함되지 않은 항목입니다.")
    private CleanType type; // 어떤 유형의 일 : 이사청소, 입주청소, 주거청소.....

    @NotBlank(message = "title cannot be empty")
    @Size(min = 5, message = "title는  최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s!?.]*$", message = "title은 대소문자, 한글, 숫자, 공백, 느낌표, 물음표만 입력 가능합니다.")
    @Schema(description = "인사말", example = "안녕하세요. 은하수 홈케어입니다.")
    private String title;


    @NotBlank(message = "titleimg cannot be empty")
    @Schema(description = "file 데이터 List", example = "file 데이터 List")
    private String titleimg; // 청소 후 사진


    @NotBlank(message = "greeting cannot be empty")
    @Size(min = 5, message = "greeting는  최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s!?.]*$", message = "문의는 대소문자, 한글, 숫자, 공백, 느낌표, 물음표만 입력 가능합니다.")
    @Schema(description = "인사말", example = "안녕하세요. 은하수 홈케어입니다.")
    private String greeting;
}
