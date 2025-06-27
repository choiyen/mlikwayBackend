package project.MilkyWay.BoardMain.Board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import project.MilkyWay.Administration.Controller.AdministrationController;
import project.MilkyWay.Administration.DTO.AdministrationDTO;
import project.MilkyWay.BoardMain.Board.Controller.BoardController;
import project.MilkyWay.BoardMain.Board.DTO.BoardCheckDTO;
import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;
import project.MilkyWay.BoardMain.Board.Service.BoardService;
import project.MilkyWay.BoardMain.Comment.Service.CommentService;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@Import(TestSecurityConfig.class)
public class BoardControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private CommentService commentService;

    ResponseDTO responseDTO = new ResponseDTO();

    @MockBean
    private LoginSuccess loginSuccess;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void testCreateBoard() throws Exception {
        // given (테스트용 mock DTO, Entity 생성)
        BoardDTO BoardmockDTO = BoardDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .title("현재 TEST 진행 중입니다.")
                .password("dfsdgq@wfw66")
                .content("어려운 내용이 많습니다.")
                .build();

        // Service.create() 호출 시 mockDTO 반환하도록 설정
        Mockito.when(boardService.Insert(any(BoardDTO.class)))
                .thenReturn(BoardmockDTO);

        // 요청 JSON (String)
        String requestJson = """
            {
                "boardId": "AFDSFBSFDSadsdds1459",
                "title": "현재 TEST 진행 중입니다.",
                "password": "dfsdgq@wfw66",
                "content": "어려운 내용이 많습니다."
            }
            """;

        // when - API 호출 및 then - 검증
        mockMvc.perform(post("/api/board")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].title").value("현재 TEST 진행 중입니다."))
                .andExpect(jsonPath("$.data[0].content").value("어려운 내용이 많습니다."));
    }

    @Test
    public void testDelete() throws Exception {
        // given
        BoardCheckDTO boardCheckDTO = BoardCheckDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .password("dfsdgq@wfw66")
                .build();

        BoardDTO BoardmockDTO = BoardDTO.builder()
                .boardId("AFDSFBSFDSadsdds1459")
                .title("현재 TEST 진행 중입니다.")
                .password("dfsdgq@wfw66")
                .content("어려운 내용이 많습니다.")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(boardCheckDTO);

        // ✅ 필수 mocking - boardService에서 boardDTO 반환
        Mockito.when(boardService.FindByBoardId(boardCheckDTO.getBoardId()))
                .thenReturn(BoardmockDTO);
        //board 컨트롤러 내에서 BoardId를 넣어서 객체를 뽑아내니까....그 부분을 mocking

        // ✅ 댓글 목록 반환 mocking
        Mockito.when(commentService.FindByBoardId(BoardmockDTO.getBoardId(), false))
                .thenReturn(Collections.emptyList());
        //댓글 목록이 존재하는지 여부 확인 - 있으면 동시 삭제 진행
        // 댓글 목록 없다고 모킹
        Mockito.when(commentService.FindByBoardId(anyString(), eq(false)))
                .thenReturn(Collections.emptyList());

        // (필요하면) 댓글 삭제도 모킹
        Mockito.when(commentService.Delete(anyLong()))
                .thenReturn(true);


        // ✅ 삭제 mocking
        Mockito.when(boardService.Delete(boardCheckDTO.getBoardId()))
                .thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/board")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("데이터 삭제 완료! 못미더우시면 DB를 확인해봐요!"));
    }
    //mockMvc를 통한 Test 시 코드 작성했을 때 쓸 모든 조건에 대하여  Mockito.when으로 test를 진행해야 한다.
    //단, donoting으로 해당 service의 함수가 void일 때 사용할 수 있다.
}
