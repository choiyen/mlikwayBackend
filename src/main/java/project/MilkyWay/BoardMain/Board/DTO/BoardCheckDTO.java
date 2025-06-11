package project.MilkyWay.BoardMain.Board.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class BoardCheckDTO {
    @Schema(description = "게시판 정보 Id", example = "dfasfdsfsafasfwv!ED")
    @JsonProperty("boardId")
    private String boardId; // 게시판 질문의 식별자

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 15, message = "비밀번호는 최소 8자리 이상 15자리 이하의 문자여야 함")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+={}|\\[\\]:;\"'<>,.?/`~-]).+$", message = "대소문자, 숫자, 특수문자가 모두 포함된 문자열만 입력 가능합니다.")
    @Schema(description = "관리자를 제어하기 위한 비밀번호", example = "asdZXC@123")
    @JsonProperty("password")
    private String password; //비밀번호
}
