package project.MilkyWay.Administration.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.Enum.EnumValue;

import java.time.LocalDate;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class AdministrationDTO
{
    @Schema(description = "일정 번호", example = "dfsdfwwf@!DASFXA")
    private String administrationId; //일정표를 관리하기 위한 id

    @Schema(description = "일정 날짜", example = "2025-03-14")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("administrationDate")
    private LocalDate administrationDate; //일정을 기록할 id

    @Schema(description = "일정 유형", example = "휴일")
    @EnumValue(enumClass = DateType.class, message = "허용되지 않은 일정 타입입니다.")
    @JsonProperty("adminstrationType")
    private DateType adminstrationType; // 일정의 유형 - 휴일, 일하는 날, 현재 비어있는 날
}
