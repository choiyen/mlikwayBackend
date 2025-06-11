package project.MilkyWay.noticeMain.Common.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.EnumValue;

@Getter
@Builder
@ToString
public class TypeDTO {
    @Schema(description = "어떤 유형의 일?", example = "이사청소")
    @EnumValue(enumClass = CleanType.class, message = "청소 리스트에 포함되지 않은 항목입니다.")
    private CleanType type; // 어떤 유형의 일 : 이사청소, 입주청소, 주거청소.....
}
