package project.MilkyWay.Administration.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.Config.DatabaseConverter;
import project.MilkyWay.Reservation.Entity.ReservationEntity;
import project.MilkyWay.ComonType.Enum.DateType;

import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(name = "administration" )
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class AdministrationEntity
{
    @Id
    @Column(name= "administrationId")
    @Comment("PK")
    private String administrationId; //일정표를 관리하기 위한 id

    @Column(name= "Date", nullable = false, unique = true)
    @Comment("일정")
    private LocalDate administrationDate; //일정을 기록할 id

    @Column(name= "Type", nullable = false)
    private DateType adminstrationType; // 일정의 유형 - 휴일, 일하는 날, 현재 비어있는 날

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "administrationId", referencedColumnName = "administrationId", updatable = false, insertable = false)
    private ReservationEntity reservationEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdministrationEntity AdministrationEntity = (AdministrationEntity) o;
        return Objects.equals(administrationId, AdministrationEntity.administrationId) &&
                Objects.equals(administrationDate, AdministrationEntity.administrationDate) &&
                Objects.equals(adminstrationType, AdministrationEntity.adminstrationType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(administrationId, administrationDate, adminstrationType);
    }

}
