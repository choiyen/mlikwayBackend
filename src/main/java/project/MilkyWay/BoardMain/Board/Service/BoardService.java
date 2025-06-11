package project.MilkyWay.BoardMain.Board.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;
import project.MilkyWay.BoardMain.Board.Repository.BoardRepository;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;


import java.util.List;

@Service
public class BoardService
{
    @Autowired
    BoardRepository boardRepository;


    public BoardEntity Insert(BoardEntity boardEntity)
    {
        BoardEntity BoardEntity1 = boardRepository.save(boardEntity);
        if(BoardEntity1 != null)
        {
            return BoardEntity1;
        }
        else
        {
            throw new InsertFailedException("날짜 가능 문의 등록이 실패하였습니다.");
        }
    }
    public BoardEntity Update(BoardEntity boardEntity)
    {
        BoardEntity BeforeBoardEntity = boardRepository.findByBoardId(boardEntity.getBoardId());
        if(BeforeBoardEntity != null)
        {
            BoardEntity AfterBoardEntity = ConvertToEntity(BeforeBoardEntity, boardEntity);
            BoardEntity boardEntity1 = boardRepository.save(AfterBoardEntity);
            BoardEntity ChangeBoardEntity = boardRepository.findByBoardId(boardEntity1.getBoardId());
            if(ChangeBoardEntity.equals(boardEntity1)) {
                return boardEntity1;
            }
            else
            {
                throw new UpdateFailedException("게시판을 변경하려고 시도했으나, 변경에 실패했습니다.");
            }
        }
        else
        {
            throw new FindFailedException("해당 코드를 가진 질문 게시판을 찾을 수 없습니다.");
        }

    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean Delete(String EncodingBoardId)
    {
        boolean bool = boardRepository.existsByBoardId(EncodingBoardId);
        if (bool)
        {
            boardRepository.deleteByBoardId(EncodingBoardId);
            boolean bool2 = boardRepository.existsByBoardId(EncodingBoardId);
            if(bool2)
            {
                throw new DeleteFailedException("게시판 삭제를 시도했는데, 삭제가 되지 않고 남아있어요");
            }
            else
            {
                return bool;
            }
        }
        else
        {
            throw new FindFailedException("해당 코드로 삭제할 수 있는 게시판이 존재하지 않아요");
        }
    }
    public Page<BoardEntity> FindAll(int page)
    {

        Pageable pageable = PageRequest.of(page, 10);
        Page<BoardEntity> boardEntity = boardRepository.findAll(pageable);
        if(boardEntity != null)
        {
            return  boardEntity;
        }
        else
        {
            throw new FindFailedException("전체 게시판 데이터를 찾을 수 없었어요. 알 수 없는 오류!!");
        }
    }
    public BoardEntity FindByBoardId(String EncodingBoardId)
    {
        BoardEntity boardEntity = boardRepository.findByBoardId(EncodingBoardId);
            return boardEntity;
    }
    public boolean Bool(String EncodingBoardId)
    {
        return boardRepository.existsByBoardId(EncodingBoardId);
    }
    private BoardEntity ConvertToEntity(BoardEntity beforeBoardEntity, BoardEntity boardEntity)
    {
        return BoardEntity.builder()
                .boardId(beforeBoardEntity.getBoardId())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .password(boardEntity.getPassword())
                .build();
    }
}
