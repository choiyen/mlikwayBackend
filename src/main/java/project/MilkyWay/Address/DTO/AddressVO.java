package project.MilkyWay.Address.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class AddressVO
{
    @Schema(description  = "고객 정보 Id", example = "dfasfdsfsafasfwv!ED")
    @JsonProperty("addressId")
    private String addressId;
}
