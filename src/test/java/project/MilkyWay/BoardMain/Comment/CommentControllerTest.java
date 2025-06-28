package project.MilkyWay.BoardMain.Comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.annotations.Comment;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import project.MilkyWay.BoardMain.Board.Controller.BoardController;
import project.MilkyWay.BoardMain.Board.DTO.BoardCheckDTO;
import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;
import project.MilkyWay.BoardMain.Board.Repository.BoardRepository;
import project.MilkyWay.BoardMain.Board.Service.BoardService;
import project.MilkyWay.BoardMain.Comment.Controller.CommentController;
import project.MilkyWay.BoardMain.Comment.DTO.CommentDTO;
import project.MilkyWay.BoardMain.Comment.DTO.CommentDeteteDTO;
import project.MilkyWay.BoardMain.Comment.Service.CommentService;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.UserType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BoardRepository boardRepository;

    ResponseDTO responseDTO = new ResponseDTO();

    @MockBean
    private LoginSuccess loginSuccess;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());



    @Test
    public void testCreateComment() throws Exception
    {
        // given (테스트용 mock DTO, Entity 생성)
        BoardDTO BoardmockDTO = BoardDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .title("현재 TEST 진행 중입니다.")
                .password("dfsdgq@wfw66")
                .content("어려운 내용이 많습니다.")
                .build();

        CommentDTO CommentmockDTO = CommentDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .commentId(1L)
                .comment("dfsdfsdfsddf")
                .password("dfsdgq@wfw66")
                .type(UserType.고객)
                .build();

        Mockito.when(boardService.FindByBoardId(CommentmockDTO.getBoardId()))
                .thenReturn(BoardmockDTO);

        // Service.create() 호출 시 mockDTO 반환하도록 설정
        Mockito.when(commentService.Insert(any(CommentDTO.class), anyString()))
                .thenReturn(CommentmockDTO);

        Mockito.when(boardRepository.existsByBoardId(CommentmockDTO.getBoardId()))
                .thenReturn(true);

        // 요청 JSON (String)
        String requestJson = """
            {
                "boardId": "AFDSFBSFDSadsdds1459",
                "comment": "현재 TEST 진행 중입니다.",
                "password":"dfsdgq@wfw66",
                "type": "고객"
            }
            """;

        // when - API 호출 및 then - 검증
        mockMvc.perform(post("/api/comment")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].comment").value("dfsdfsdfsddf"))
                .andExpect(jsonPath("$.data[0].type").value("고객"));
    }
    @Test
    public void testDelete() throws Exception {
        // given
        CommentDeteteDTO commentDeteteDTO = CommentDeteteDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .commentId(1L)
                .password("dfsdgq@wfw66")
                .build();

        CommentDTO CommentmockDTO = CommentDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .commentId(1L)
                .comment("dfsdfsdfsddf")
                .password("dfsdgq@wfw66")
                .type(UserType.고객)
                .build();

        BoardDTO BoardmockDTO = BoardDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .title("현재 TEST 진행 중입니다.")
                .password("dfsdgq@wfw66")
                .content("어려운 내용이 많습니다.")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(commentDeteteDTO);

        //board 컨트롤러 내에서 BoardId를 넣어서 객체를 뽑아내니까....그 부분을 mocking
        Mockito.when(boardService.FindByBoardId(CommentmockDTO.getBoardId()))
                        .thenReturn(BoardmockDTO);
        // (필요하면) 댓글 삭제도 모킹
        Mockito.when(commentService.Delete(CommentmockDTO.getCommentId()))
                .thenReturn(true);


        // when & then
        mockMvc.perform(delete("/api/comment")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("데이터 삭제!"));
    }

    @Test
    public void testFindByBoardId_Success() throws Exception {
        String boardId = "dfsdfw@656";
        CommentDTO CommentmockDTO = CommentDTO.builder()
                .boardId("dfsdfw@656")
                .commentId(1L)
                .comment("dfsdfsdfsddf")
                .password("dfsdgq@wfw66")
                .type(UserType.고객)
                .build();
        // 세션이 있다고 가정
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        Mockito.when(commentService.FindByBoardId(eq(boardId), anyBoolean()))
                .thenReturn(Collections.singletonList(CommentmockDTO));

        mockMvc.perform(get("/api/comment/search/panel")
                        .param("BoardId", boardId)
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.data[0].boardId").value(boardId));
    }

}
