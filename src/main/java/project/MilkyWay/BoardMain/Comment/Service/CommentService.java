package project.MilkyWay.BoardMain.Comment.Service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.BoardMain.Board.Repository.BoardRepository;
import project.MilkyWay.BoardMain.Comment.DTO.CommentDTO;
import project.MilkyWay.BoardMain.Comment.Entity.CommentEntity;
import project.MilkyWay.ComonType.Enum.UserType;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;

import project.MilkyWay.BoardMain.Comment.Repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService
{
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    BoardRepository boardRepository;


    public CommentDTO Insert(CommentDTO comment, String commentType)
    {

             CommentEntity commentEntity = ConvertToCommentEntity(comment, commentType);
             boolean bool = boardRepository.existsByBoardId(comment.getBoardId());
            if(bool)
            {
                return ConvertToCommentDTO(commentRepository.save(commentEntity));
            }
            else
            {
                throw new FindFailedException("추가하는데 필요한 질문 게시판 정보를 찾을 수 없어요.");
            }

    }
    public CommentDTO Update(CommentDTO comment)
    {

        CommentEntity commentEntity = ConvertToCommentEntity(comment);
        CommentEntity oldComment = commentRepository.findByCommentId(comment.getCommentId());
           if(commentEntity != null)
           {
                CommentEntity UpdateComment = ConvertToEntity(oldComment, commentEntity);
                CommentEntity commentEntity1 = commentRepository.save(UpdateComment);
                CommentEntity ChangeCommentEntity = commentRepository.findByCommentId(commentEntity1.getCommentId());
                if(ChangeCommentEntity.equals(commentEntity1)) {
                    return ConvertToCommentDTO(commentEntity1);
                }
                else
                {
                    throw new UpdateFailedException("질문 게시판에 남긴 데이터의 변경을 시도했으나, 실패했습니다.");
                }
           }
           else
           {
               throw new FindFailedException("댓글 내역을 찾을 수 없습니다.");
           }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean Delete(Long EnCodingCommentId)
    {
        boolean bool = commentRepository.existsByCommentId(EnCodingCommentId);
        if(bool)
        {
            commentRepository.deleteByCommentId(EnCodingCommentId);
            return true;
        }
        else
        {
            throw new DeleteFailedException("해당 CommentId를 가진 질문이 존재하지 않습니다.");
        }
    }
    public CommentDTO FindByCommentId(Long EnCodingCommentId)
    {
        CommentEntity commentEntity = commentRepository.findByCommentId(EnCodingCommentId);
        if(commentEntity != null)
        {
            return ConvertToCommentDTO(commentEntity);
        }
        else
        {
            throw new FindFailedException("코멘트 Id에 따른 데이터를 찾는데 오류가 발생했습니다. 다시 시도해주세요");
        }

    }
    public List<CommentDTO> FindByBoardId(String EnCodingBoardId, boolean bool)
    {
        List<CommentEntity> commentEntities = new ArrayList<>(commentRepository.findByBoardId(EnCodingBoardId));

        if(bool == true && commentEntities.isEmpty())
        {
            throw new FindFailedException("데이터 조회에는 성공하였으나, 조회 결과가 없습니다.");
        }

        if(commentEntities != null)
        {
            List<CommentDTO> commentDTOS = new ArrayList<>();
            for(CommentEntity comment : commentEntities)
            {
                commentDTOS.add(ConvertToCommentDTO(comment));
            }

            return commentDTOS;
        }
        else
        {
            throw  new FindFailedException("알 수 없는 오류로 데이터 조회에 실패하였습니다.");
        }
    }
    private CommentEntity ConvertToEntity(CommentEntity Oldcomment, CommentEntity newcomment)
    {
        return CommentEntity.builder()
                .boardId(Oldcomment.getBoardId())
                .commentId(Oldcomment.getCommentId())
                .comment(newcomment.getComment())
                .type(Oldcomment.getType())
                .createdAt(newcomment.getCreatedAt())
                .build();
    }


    private CommentDTO ConvertToCommentDTO(CommentEntity comment)
    {
        return CommentDTO.builder()
                .type(UserType.valueOf(comment.getType()))
                .commentId(comment.getCommentId())
                .boardId(comment.getBoardId())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private CommentEntity ConvertToCommentEntity(@Valid CommentDTO commentDTO)
    {
        return CommentEntity.builder()
                .type(String.valueOf(commentDTO.getType()))
                .commentId(commentDTO.getCommentId())
                .boardId(commentDTO.getBoardId())
                .comment(commentDTO.getComment())
                .createdAt(commentDTO.getCreatedAt())
                .build();
    }
    private CommentEntity ConvertToCommentEntity(@Valid CommentDTO commentDTO, String provider)
    {
        return CommentEntity.builder()
                .type(provider)
                .commentId(commentDTO.getCommentId())
                .boardId(commentDTO.getBoardId())
                .comment(commentDTO.getComment())
                .createdAt(commentDTO.getCreatedAt())
                .build();
    }


}
