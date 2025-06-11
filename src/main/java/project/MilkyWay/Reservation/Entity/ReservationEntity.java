package project.MilkyWay.Reservation.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.Config.DatabaseConverter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "reservation")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class ReservationEntity
{
    @Id
    @Column(name = "reservationId")
    @Comment("PK")
    private String reservationId; //예약을 관리하기 위한 ID

    @Column(name = "administrationId")
    @Comment("EK")
    private String administrationId;

    @Column(name = "name")
    @Comment("고객이름")
    private String name;
    // 고객의 이름
    @Column(name = "phone")
    @Comment("전화번호")
    private String phone; // 전화번호

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("청소유형")
    private CleanType type; // 어떤 유형의 일 : 이사청소, 입주청소, 주거청소.....

    @Column(name = "acreage") //실 평수
    @Comment("실평수")
    private String acreage;

    @Column(name = "Address")
    @Comment("주소")
    private String address; // 주소(암호화 처리 필요)

    @Column(name = "subissionDate")
    @Comment("예약날짜")
    private LocalDate subissionDate; // 예약 날짜

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationEntity reservationEntity = (ReservationEntity) o;
        return Objects.equals(reservationId, reservationEntity.reservationId) &&
                Objects.equals(administrationId, reservationEntity.administrationId) &&
                Objects.equals(acreage, reservationEntity.acreage) &&
                Objects.equals(name, reservationEntity.name) &&
                Objects.equals(phone, reservationEntity.phone) &&
                Objects.equals(address, reservationEntity.address) &&
                Objects.equals(subissionDate, reservationEntity.subissionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, administrationId, acreage, name, phone, address, subissionDate);
    }

}
