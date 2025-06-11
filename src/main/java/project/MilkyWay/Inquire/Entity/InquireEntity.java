package project.MilkyWay.Inquire.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.Config.DatabaseConverter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "Inqurie")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class InquireEntity
{
    @Id
    @Column(name = "inquireId")
    @Comment("PK")
    private String inquireId;

    @Column(name = "address", nullable = false)
    @Comment("주소")
    @Convert(converter = DatabaseConverter.class)
    private String address;

    @Column(name = "phoneNumber", nullable = false)
    @Comment("전화번호")
    @Convert(converter = DatabaseConverter.class)
    private String phoneNumber;

    @Column(name = "inquirename", nullable = false)
    @Comment("회원 이름")
    @Convert(converter = DatabaseConverter.class)
    private String inquirename;

    @Column(name = "inquire", nullable = false)
    @Comment("고객 문의")
    @Convert(converter = DatabaseConverter.class)
    private String inquire;

    @Column(name = "dateOfInquiry", nullable = false)
    @Comment("문의날짜")
    private LocalDate dateOfInquiry;

    @Column(name = "inquireBool", nullable = false)
    @Comment("문의처리여부")
    private Boolean inquireBool;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InquireEntity InquireEntity = (InquireEntity) o;
        return Objects.equals(inquireId, InquireEntity.inquireId) &&
                Objects.equals(address, InquireEntity.address) &&
                Objects.equals(phoneNumber, InquireEntity.phoneNumber) &&
                Objects.equals(inquire, InquireEntity.inquire);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inquireId, address, phoneNumber, inquire);
    }
}
//고객의 상담을 받기 위한 Entity니까, 따로 묶을 이유는 없음(그 날짜가 상담 가능한 날짜인지만 확인하면 됨)
