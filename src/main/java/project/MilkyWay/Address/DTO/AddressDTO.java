package project.MilkyWay.Address.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.EnumValue;
import project.MilkyWay.ComonType.Enum.UserType;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class AddressDTO
{
    @Schema(description  = "고객 정보 Id", example = "dfasfdsfsafasfwv!ED")
    private String addressId;

    @NotBlank(message = "customer cannot be empty")
    @Size(min = 3, message = "customer는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z가-힣]*$", message = "고객 이름은 대소문자, 한글만 입력 가능합니다.")
    @Schema(description = "고객 이름", example = "홍길동")
    private String customer;

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 5, message = "Address는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]*$", message = "주소는 대소문자, 한글, 숫자, 공백만 입력 가능합니다.")
    @Schema(description = "고객 주소", example = "한국시 아주동 한국파크 101동 4121호")
    private String address;

    @NotBlank(message = "phoneNumber cannot be empty")
    @Size(min = 5, message = "phoneNumber는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[0-9-]*$", message = "전화번호는 숫자, 하이픈만 입력 가능합니다.")
    @Schema(description = "고객 전화번호", example = "010-1234-1234")
    private String phoneNumber;



    @Schema(description = "예약 날짜", example = "2025-03-14")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate submissionDate;

    @NotBlank(message = "acreage cannot be empty")
    @Pattern(regexp = "^[0-9]+평$", message = "평수는 숫자와 '평'만 입력 가능합니다.")
    private String acreage;


    @NotNull(message = "type cannot be empty")
    @Schema(description = "어떤 주거 요청 타입인가?", example = "주거청소")
    @EnumValue(enumClass = CleanType.class, message = "허용되지 않은 CleanType 타입입니다.")
    private CleanType cleanType; // 청소 유형을 판단함

}
//- 현재 날짜보다 고객의 의뢰 날짜가 뒷날일 떄 데이터를 파기하는 함수 필요
//고객 관리를 위한 목적의 DTO


