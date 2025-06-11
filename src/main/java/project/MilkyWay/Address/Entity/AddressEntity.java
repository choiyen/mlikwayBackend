package project.MilkyWay.Address.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.Config.DatabaseConverter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "Address")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class AddressEntity
{
    @Id
    @Column(name = "addressId", nullable = false)
    @Comment("PK")
    private String addressId;

    @Column(name = "customer", nullable = false)
    @Comment("고객이름")
    @Convert(converter = DatabaseConverter.class)
    private String customer;

    @Column(name = "Address", nullable = false)
    @Comment("주소")
    @Convert(converter = DatabaseConverter.class)
    private String address;

    @Column(name = "phoneNumber", nullable = false)
    @Comment("전화번호")
    @Convert(converter = DatabaseConverter.class)
    private String phoneNumber;

    @Column(name = "SubmissionDate", nullable = false)
    @Comment("예약 일자")
    private LocalDate submissionDate;

    @Column(name = "acreage", nullable = false)
    @Convert(converter = DatabaseConverter.class)
    private String acreage;

    @Column(name = "cleanType", nullable = false)
    private CleanType cleanType; // 청소 유형을 판단함


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressEntity AddressEntity = (AddressEntity) o;
        return Objects.equals(addressId, AddressEntity.addressId) &&
                Objects.equals(customer, AddressEntity.customer) &&
                Objects.equals(address, AddressEntity.address) &&
                Objects.equals(phoneNumber, AddressEntity.phoneNumber) &&
                Objects.equals(submissionDate, AddressEntity.submissionDate) && Objects.equals(acreage, AddressEntity.acreage)&& Objects.equals(cleanType, AddressEntity.cleanType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(addressId, customer, address,phoneNumber, submissionDate, acreage);
    }

}
