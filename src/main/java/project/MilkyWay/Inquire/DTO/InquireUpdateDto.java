package project.MilkyWay.Inquire.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class InquireUpdateDto {
    @JsonProperty("InqurieId")
    private String InqurieId;
}
