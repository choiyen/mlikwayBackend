package project.MilkyWay.BoardMain.Board.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, String>
{
    BoardEntity findByBoardId(String boardId);
    boolean existsByBoardId(String boardId);
    void deleteByBoardId(String boardId);
    Page<BoardEntity> findAll(Pageable pageable);
}
