package project.MilkyWay.Address.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.MilkyWay.Address.Entity.AddressEntity;
import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, String>
{
    AddressEntity findByAddressId(String addressId);
    AddressEntity findBySubmissionDate(LocalDate SubmissionDate);
    boolean existsBySubmissionDate(LocalDate SubmissionDate);
    boolean existsByAddressId(String addressId);
    void deleteByAddressId(String addressId);
    Page<AddressEntity> findAll(Pageable pageable);
    void deleteBySubmissionDateBefore(LocalDate dateTime);

}
