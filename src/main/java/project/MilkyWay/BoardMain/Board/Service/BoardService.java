package project.MilkyWay.BoardMain.Board.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;
import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;
import project.MilkyWay.BoardMain.Board.Repository.BoardRepository;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.ComonType.LoginSuccess;


import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService
{
    @Autowired
    BoardRepository boardRepository;


    public BoardDTO Insert(BoardDTO boardDTO)
    {
        String uniqueId;
        LoginSuccess loginSuccess = new LoginSuccess();
        do
        {
            uniqueId = loginSuccess.generateRandomId(15);
            BoardDTO boardDTO1 = FindByBoardId(uniqueId);
            if(boardDTO1 == null)
            {
                break;
            }
        }while (true);

        BoardEntity boardEntity = ConvertToBoardEntity(boardDTO, uniqueId);
        BoardEntity savedEntity = boardRepository.save(boardEntity);
        if(savedEntity != null)
        {
            return ConvertToBoardDTO(savedEntity);
        }
        else
        {
            throw new InsertFailedException("날짜 가능 문의 등록이 실패하였습니다.");
        }
    }
    public BoardDTO Update(BoardDTO boardDTO)
    {
        BoardEntity boardEntity = ConvertToBoardEntity(boardDTO);
        BoardEntity BeforeBoardEntity = boardRepository.findByBoardId(boardEntity.getBoardId());
        if(BeforeBoardEntity != null)
        {
            BoardEntity AfterBoardEntity = ConvertToEntity(BeforeBoardEntity, boardEntity);
            BoardEntity boardEntity1 = boardRepository.save(AfterBoardEntity);
            BoardEntity ChangeBoardEntity = boardRepository.findByBoardId(boardEntity1.getBoardId());
            if(ChangeBoardEntity.equals(boardEntity1)) {
                return ConvertToBoardDTO(boardEntity1);
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
    public PageDTO FindAll(int page)
    {

        Pageable pageable = PageRequest.of(page, 10);
        Page<BoardEntity> boardEntitys = boardRepository.findAll(pageable);
        List<BoardDTO> boardDTOS = new ArrayList<>();
        for(BoardEntity boardEntity : boardEntitys) {
            boardDTOS.add(ConvertToBoardDTO(boardEntity));
        }

        if(boardDTOS != null)
        {
            PageDTO pageDTO = PageDTO.<BoardDTO>builder().list(boardDTOS)
                    .PageCount(boardEntitys.getTotalPages())
                    .Total(boardEntitys.getTotalElements()).build();

            return  pageDTO;
        }
        else
        {
            throw new FindFailedException("전체 게시판 데이터를 찾을 수 없었어요. 알 수 없는 오류!!");
        }
    }
    public BoardDTO FindByBoardId(String EncodingBoardId)
    {
        BoardEntity boardEntity = boardRepository.findByBoardId(EncodingBoardId);
        if(boardEntity != null)
        {
            return ConvertToBoardDTO(boardRepository.findByBoardId(EncodingBoardId));
        }
        else
        {
            return null;
        }
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
    private BoardDTO ConvertToBoardDTO(BoardEntity boardEntity1)
    {
        return BoardDTO.builder()
                .boardId(boardEntity1.getBoardId())
                .content(boardEntity1.getContent())
                .title(boardEntity1.getTitle())
                .password(boardEntity1.getPassword())
                .build();
    }

    private BoardEntity ConvertToBoardEntity(BoardDTO boardDTO)
    {
        return BoardEntity.builder()
                .boardId(boardDTO.getBoardId())
                .content(boardDTO.getContent())
                .title(boardDTO.getTitle())
                .password(boardDTO.getPassword())
                .build();
    }
    private BoardEntity ConvertToBoardEntity(BoardDTO boardDTO, String uniqueId)
    {
        return BoardEntity.builder()
                .boardId(uniqueId)
                .content(boardDTO.getContent())
                .title(boardDTO.getTitle())
                .password(boardDTO.getPassword())
                .build();
    }
}
