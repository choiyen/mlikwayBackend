package project.MilkyWay.BoardMain.Board.Controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import project.MilkyWay.BoardMain.Board.DTO.BoardCheckDTO;
import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;

import project.MilkyWay.BoardMain.Board.Entity.BoardEntity;
import project.MilkyWay.BoardMain.Board.Service.BoardService;
import project.MilkyWay.BoardMain.Comment.Entity.CommentEntity;
import project.MilkyWay.BoardMain.Comment.Service.CommentService;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.LoginSuccess;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/board")
@Tag(name = "게시판 관련 정보를 제공하는  Controller")
public class BoardController
{//1차 Test 완료
    @Autowired
    BoardService boardService;

    @Autowired
    CommentService commentService;

    private final ResponseDTO<BoardDTO> responseDTO = new ResponseDTO<>();


    @Operation(
            summary =  "Create a new Board",
            description = "This API creates a new Board and returns BoardDTO as response",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Board created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<?> Insert(@Valid @RequestBody BoardDTO boardDTO)
    {
        try
        {
            String uniqueId;
            LoginSuccess loginSuccess = new LoginSuccess();
            do
            {
                uniqueId = loginSuccess.generateRandomId(15);
                BoardEntity boardEntity = boardService.FindByBoardId(uniqueId);
                if(boardEntity == null)
                {
                    break;
                }
            }while (true);

            BoardEntity boardEntity = ConvertToBoardEntity(boardDTO, uniqueId);
            BoardEntity boardEntity1 = boardService.Insert(boardEntity);
            if(boardEntity1 != null)
            {
                BoardDTO boardDTO1 = ConvertToBoardDTO(boardEntity1);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success","게시판 데이터 추가 완료", Collections.singletonList(boardDTO1)));
            }
            else
            {
                throw new InsertFailedException("데이터베이스에 데이터를 저장하는데 실패했습니다. 알 수 없는 오류가 발생했어요.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @Operation(
            summary = "Change a Board by BoardId",  // Provide a brief summary
            description = "This API Change a  Board and returns BoardDTO as response",  // Provide detailed description
            responses = {
                    @ApiResponse(responseCode = "201", description = "Board Changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardDTO.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid Change data"
                    )
            }
    )
    @PutMapping("/Update")
    public ResponseEntity<?> Update(@Valid @RequestBody BoardDTO boardDTO)
    {
        try
        {
            BoardEntity boardEntity = ConvertToBoardEntity(boardDTO);
            BoardEntity boardEntity2 = boardService.Update(boardEntity);
            if(boardEntity2 != null)
            {
                BoardDTO boardDTO1 = ConvertToBoardDTO(boardEntity2);
                return ResponseEntity.ok().body(responseDTO.Response("success","게시판 데이터 업데이트 완료", Collections.singletonList(boardDTO1)));
            }
            else
            {
                throw new InsertFailedException("데이터베이스의 데이터를 수정하는데 실패했습니다. 알 수 없는 오류가 발생했어요.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Delete an Board by BoardId",  // Provide a brief summary
            description = "This API deletes an Board by the provided BoardId and returns a ResponseEntity with a success or failure message.",  // Provide detailed description
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Board deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Board not found"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<?> Delete(@RequestBody BoardCheckDTO boardCheckDTO)
    {
        try
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();  // 사용자 이름

            BoardEntity boardEntity = boardService.FindByBoardId(boardCheckDTO.getBoardId());
            if(username.equals("anonymousUser") && !boardEntity.getPassword().equals(boardCheckDTO.getPassword()))
            {
                throw new RuntimeException("삭제를 위한 비밀번호를 다시 입력해주세요");
            }
            List<CommentEntity> list = new ArrayList<>(commentService.FindByBoardId(boardEntity.getBoardId(), false));
            if(!list.isEmpty())
            {
                for(CommentEntity comment : list)
                {
                    commentService.Delete(comment.getCommentId());
                }
            }

            boolean bool = boardService.Delete(boardCheckDTO.getBoardId());
            if(bool)
            {
                return ResponseEntity.ok().body(responseDTO.Response("success","데이터 삭제 완료! 못미더우시면 DB를 확인해봐요!"));
            }
            else
            {
               throw new DeleteFailedException("데이터 삭제에 알 수 없는 오류로 실패했어요!!");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));

        }
    }


    @Operation(
            summary = "Returns a list of BoardDTO objects",
            description = "This API retrieves a list of BoardDTO objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Board List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Board List not found")
            }
    )
    @GetMapping("/search/page")
    public ResponseEntity<?> FindALl(@RequestParam(value = "page", defaultValue = "0") int page)
    {
        try
        {

            Page<BoardEntity> boardEntities = boardService.FindAll(page);
            List<BoardDTO> boardDTOS = new ArrayList<>();
            for(BoardEntity boardEntity : boardEntities) {
                boardDTOS.add(ConvertToBoardDTO(boardEntity));
            }
            if(boardDTOS.isEmpty())
            {
                return ResponseEntity.ok().body(responseDTO.Response("empty","데이터베이스에 내용은 비어있음"));
            }
            else {
                PageDTO pageDTO = PageDTO.<BoardDTO>builder().list(boardDTOS)
                        .PageCount(boardEntities.getTotalPages())
                        .Total(boardEntities.getTotalElements()).build();
                return ResponseEntity.<PageDTO>ok().body(responseDTO.Response("success","데이터 조회 완료!", pageDTO));
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Returns BoardDTO object for a given Board Id",
            description = "This API retrieves an Board based on the provided Board Id and returns the corresponding BoardDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Board found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Board not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> FindByBoardId(@RequestParam String BoardId)
    {
        try 
        {
            BoardEntity boardEntity = boardService.FindByBoardId(BoardId);
            if(boardEntity != null)
            {
                BoardDTO boardDTO1 = ConvertToBoardDTO(boardEntity);
                return ResponseEntity.ok().body(responseDTO.Response("success","게시판 데이터 조회완료", Collections.singletonList(boardDTO1)));

            }
            else
            {
                throw new RuntimeException("데이터를 찾는 도중에 알 수 없는 오류로 DB를 조회할 수 없습니다.");
            }
        } 
        catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
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
