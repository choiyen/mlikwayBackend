package project.MilkyWay.BoardMain.Comment.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import project.MilkyWay.ComonType.Enum.EnumValue;
import project.MilkyWay.ComonType.Enum.UserType;

import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CommentDTO
{
    @Schema(description = "답변 정보 Id", example = "444")
    private Long commentId; // 답변을 구분하기 위한 id

    @NotBlank(message = "boardId cannot be empty")
    @Schema(description = "게시판 정보 Id", example = "dfasfdsfsafasfwv!ED")
    private String boardId;  // boardDTO와 연결하기 위한 것

    @NotNull(message = "type cannot be empty")
    @Schema(description = "댓글을 단 사람의 유형", example = "관리자")
    @EnumValue(enumClass = UserType.class, message = "허용되지 않은 유저 타입입니다.")
    private UserType type; // 관리자인지, 사용자인지? 로그인 시에는 관리자, 비로그인 시에는 고객으로 판단

    @NotBlank(message = "type cannot be empty")
    @Size(min = 5, message = "type는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣.,?!& ]*$", message = "comment은 대소문자, 한글, 숫자, 특수문자(.,?!& )만 입력 가능합니다.")
    @Schema(description = "댓글 내용", example = "네, 요구사항은 대부분 반영되나, 추가 요금이 있을 수 있습니다.")
    private String comment; // 댓글을 저장하기 위한 변수

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 15, message = "비밀번호는 최소 8자리 이상 15자리 이하의 문자여야 함")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+={}|\\[\\]:;\"'<>,.?/`~-]).+$", message = "대소문자, 숫자, 특수문자가 모두 포함된 문자열만 입력 가능합니다.")
    @Schema(description = "관리자를 제어하기 위한 비밀번호", example = "asdZXC@123")
    @JsonProperty("password")
    private String password;

    @Schema(description = "댓글 작성 시간", example = "2024-05-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date createdAt;

}
