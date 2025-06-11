package project.MilkyWay.Inquire.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;
import project.MilkyWay.Inquire.Entity.InquireEntity;



@Repository
public interface InqurieRepository extends JpaRepository<InquireEntity, String>
{
    InquireEntity findByInquireId(String InquireId);
    boolean existsByInquireId(String InquireId);
    void deleteByInquireId(String InqurieId);
    Page<InquireEntity> findAll(Pageable pageable);
}
