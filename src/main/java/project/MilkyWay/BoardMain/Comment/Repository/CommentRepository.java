package project.MilkyWay.BoardMain.Comment.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.MilkyWay.BoardMain.Comment.Entity.CommentEntity;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, String>
{

    CommentEntity findByCommentId(Long commentId);
    List<CommentEntity> findByBoardId(String BoardId);
    boolean existsByCommentId(Long commentId);
    void deleteByCommentId(Long commentId);
}
