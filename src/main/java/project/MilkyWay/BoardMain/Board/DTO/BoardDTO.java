package project.MilkyWay.BoardMain.Board.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class BoardDTO {

    @Schema(description = "게시판 정보 Id", example = "dfasfdsfsafasfwv!ED")
    @JsonProperty("boardId")
    private String boardId; // 게시판 질문의 식별자

    @NotBlank(message = "title cannot be empty")
    @Size(min = 5, message = "title는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣.,?!& ]*$", message = "제목은 대소문자, 한글, 숫자, 특수문자(.,?!& )만 입력 가능합니다.")
    @Schema(description = "타이틀 이름", example = "아이가 아토피가 있는데, 이런 요구사항도 반영해주시나요?")
    @JsonProperty("title")
    private String title; // 게시판 질문의 제목을 저장하는 변수(공백 설정 주의)

    @NotBlank(message = "content cannot be empty")
    @Size(min = 5, message = "content는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣., ]*$", message = "내용은 대소문자, 한글, 숫자, 점(.), 쉼표(,), 공백만 입력 가능합니다.")
    @Schema(description = "게시판 글", example = "내용은 제목과 동일합니다.")
    @JsonProperty("content")
    private String content; // 게시판의 내용을 저장하는 변수


    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 15, message = "비밀번호는 최소 8자리 이상 15자리 이하의 문자여야 함")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+={}|\\[\\]:;\"'<>,.?/`~-]).+$", message = "대소문자, 숫자, 특수문자가 모두 포함된 문자열만 입력 가능합니다.")
    @Schema(description = "게시판을 제어하기 위한 비밀번호", example = "asdZXC@123")
    @JsonProperty("password")
    private String password; //비밀번호
}

