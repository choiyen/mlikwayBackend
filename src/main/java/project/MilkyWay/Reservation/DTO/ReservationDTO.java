package project.MilkyWay.Reservation.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.EnumValue;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ReservationDTO //고객의 예약을 관리하기 위한 DTO
{

    @Schema(description = "예약을 관리하기 위한 ID", example = "dfsfsfadf@!FSCVS")
    String reservationId; //예약을 관리하기 위한 ID

    @Schema(description = "예약일정을 관리하기 위한 Id", example = "2024-10-11") //예약 일정이 있어야 들어가는 게 아니라, 예약 일정이 없다면 생성하고, 데이터 추가 작업 수행
    String administrationId;

    @Schema(description = "어떤 유형의 일?", example = "이사청소")
    @EnumValue(enumClass = CleanType.class, message = "청소 리스트에 포함되지 않은 항목입니다.")
    private CleanType type; // 어떤 유형의 일 : 이사청소, 입주청소, 주거청소.....


    @NotBlank(message = "acreage cannot be empty")
    @Size(max= 5, message = "acreage는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[가-힣0-9]+$", message = "acreage는 한글과 숫자만 입력 가능합니다.")
    @Schema(description = "실 평수", example = "25평")
    String acreage;

    @NotBlank(message = "name cannot be empty")
    @Size(min = 2, max= 10, message = "이름은 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[가-힣0-9]+$", message = "name은 한글과 숫자만 입력 가능합니다.")
    @Schema(description = "예약 고객의 이름", example = "홍길성")
    private String name; // 고객의 이름

    @NotBlank(message = "phoneNumber cannot be empty")
    @Size(min = 5, message = "phoneNumber는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[0-9-]*$", message = "전화번호는 숫자, 하이픈만 입력 가능합니다.")
    @Schema(description = "고객 전화번호", example = "010-1234-1234")
    private String phone; // 전화번호

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 5, message = "Address는 최소 다섯자리 이상 입력해야 함.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]*$", message = "주소는 대소문자, 한글, 숫자, 공백만 입력 가능합니다.")
    @Schema(description = "고객 주소", example = "한국시 아주동 한국파크 101동 4121호")
    @JsonProperty("address")
    private String address; // 주소(암호화 처리 필요)

    @Schema(description = "예약 날짜", example = "2025-03-14")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("subissionDate")
    private LocalDate subissionDate; // 예약 날짜
}
