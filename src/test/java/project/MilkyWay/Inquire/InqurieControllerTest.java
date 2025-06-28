package project.MilkyWay.Inquire;

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
import org.springframework.test.web.servlet.MockMvc;
import project.MilkyWay.Administration.DTO.AdministrationDTO;
import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;
import project.MilkyWay.Inquire.Controller.InqurieController;
import project.MilkyWay.Inquire.DTO.InquireDTO;
import project.MilkyWay.Inquire.DTO.InquireUpdateDto;
import project.MilkyWay.Inquire.Service.InquireService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InqurieController.class)
@Import(TestSecurityConfig.class)
public class InqurieControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquireService inquireService;

    @MockBean
    private LoginSuccess loginSuccess;

    ResponseDTO responseDTO = new ResponseDTO();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void InqurieInsertTest() throws Exception
    {
        InquireDTO inquireMockDTO = InquireDTO.builder()
                .inquireId("dfsdfsd@565")
                .inquirename("홍길동")
                .address("경상남도 거제시 성북동")
                .phoneNumber("010-1234-5678")
                .dateOfInquiry(LocalDate.of(2025,10, 15))
                .inquireBool(true)
                .inquire("우리 동네에도 청소하러 오시나요")
                .build();

        Mockito.when(inquireService.Insert(any(InquireDTO.class)))
                .thenReturn(inquireMockDTO);

        String requestJson = """
            {
                "inquireId": "abc123",
                "inquirename": "홍길동",
                "address": "경상남도 거제시 성북동",
                "phoneNumber": "010-1234-5678",
                "dateOfInquiry": "2025-10-15",
                "inquireBool": true,
                "inquire": "우리 동네에도 청소하러 오시나요"
            }
            """;

        // when - API 호출 및 then - 검증
        mockMvc.perform(post("/api/inqurie")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].inquirename").value("홍길동"))
                .andExpect(jsonPath("$.data[0].phoneNumber").value("010-1234-5678"));
    }
    @Test
    public void InqurieUpdateTest() throws  Exception
    {
        InquireUpdateDto inquireUpdateMockDto = InquireUpdateDto.builder()
                .InqurieId("dfsdfsd@565")
                .build();
        InquireDTO inquireUpdatedMockDTO = InquireDTO.builder()
                .inquireId("dfsdfsd@565")
                .inquirename("홍길동")
                .address("경상남도 거제시 성북동")
                .phoneNumber("010-1234-5678")
                .dateOfInquiry(LocalDate.of(2025,10, 15))
                .inquireBool(true)
                .inquire("우리 동네에도 청소하러 오시나요")
                .build();

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        Mockito.when(inquireService.Check(inquireUpdateMockDto.getInqurieId()))
                .thenReturn(inquireUpdatedMockDTO);


        mockMvc.perform(put("/api/inqurie")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inquireUpdateMockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].inquireBool").value(true));
    }
    @Test
    public void InquireByIdTest() throws Exception {
        String InquireId = "dfsdfsd@565";

        InquireDTO inquireSelectedMockDTO = InquireDTO.builder()
                .inquireId("dfsdfsd@565")
                .inquirename("홍길동")
                .address("경상남도 거제시 성북동")
                .phoneNumber("010-1234-5678")
                .dateOfInquiry(LocalDate.of(2025,10, 15))
                .inquireBool(true)
                .inquire("우리 동네에도 청소하러 오시나요")
                .build();

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        Mockito.when(inquireService.FindByInquireId(InquireId))
                .thenReturn(inquireSelectedMockDTO);

        mockMvc.perform(get("/api/inqurie/search")
                        .param("InquireId", "dfsdfsd@565")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.data[0].inquireId").value(InquireId));
    }
    @Test
    public void testFindAllWithPageDTO() throws Exception
    {
        // given

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class)))
                .thenAnswer(invocation -> {
                    HttpServletRequest request = invocation.getArgument(0);
                    Object userId = request.getSession().getAttribute("userId");
                    return userId != null;
                });

        InquireDTO inquireSelectedMockDTO = InquireDTO.builder()
                .inquireId("dfsdfsd@565")
                .inquirename("홍길동")
                .address("경상남도 거제시 성북동")
                .phoneNumber("010-1234-5678")
                .dateOfInquiry(LocalDate.of(2025,10, 15))
                .inquireBool(true)
                .inquire("우리 동네에도 청소하러 오시나요")
                .build();

        List<InquireDTO> inquireDTOS = List.of(inquireSelectedMockDTO);

        PageDTO pageDTO = PageDTO.builder()
                .list(Collections.singletonList(inquireDTOS))
                .PageCount(1)
                .Total(100L)
                .build();

        // addressService.findAll(page) 호출 시 ResponseDTO 형태 반환
        Mockito.when(inquireService.findAll(anyInt()))
                .thenReturn(pageDTO);

        // when & then
        mockMvc.perform(get("/api/inqurie/search/page?page=0")
                        .sessionAttr("userId", "testUserId")  // 여기 세션에 userId 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageDTO.list[0][0].inquireId").value("dfsdfsd@565"));
    }

    @Test
    public void testDelete() throws Exception {
        String inquireId = "abc123";

        // 세션 존재 여부 모킹
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class)))
                .thenReturn(true);

        Mockito.when(inquireService.Delete(inquireId))
                .thenReturn(true); // 혹은 필요한 성공 값

        mockMvc.perform(delete("/api/inqurie")
                        .param("inqurieId", inquireId)
                        .sessionAttr("userId", "testUserId") // 세션 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("데이터 삭제에 성공했습니다."));
        //컨트롤러와 목업의 반환 값을 일치 시켜 줘야 한다.
    }
}
