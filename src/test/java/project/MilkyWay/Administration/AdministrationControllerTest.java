package project.MilkyWay.Administration;

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
import project.MilkyWay.Address.DTO.AddressDTO;
import project.MilkyWay.Address.Service.AddressService;
import project.MilkyWay.Administration.Controller.AdministrationController;
import project.MilkyWay.Administration.DTO.AdministrationDTO;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdministrationController.class)
@Import(TestSecurityConfig.class)
public class AdministrationControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationService administrationService;


    @MockBean
    private AddressService addressService;

    ResponseDTO responseDTO = new ResponseDTO();


    @MockBean
    private LoginSuccess loginSuccess;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @Test
    public void testCreateAddress() throws Exception {
        // given (테스트용 mock DTO, Entity 생성)
        AdministrationDTO administrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();

        // Service.create() 호출 시 mockDTO 반환하도록 설정
        Mockito.when(administrationService.insert(any(AdministrationDTO.class)))
                .thenReturn(administrationmockDTO);

        // 요청 JSON (String)
        String requestJson = """
            {
                "administrationId": "abc123",
                "administrationDate": "2025-06-25",
                "adminstrationType": "업무"
            }
            """;

        // when - API 호출 및 then - 검증
        mockMvc.perform(post("/api/time")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].administrationDate").value("2025-06-25"))
                .andExpect(jsonPath("$.data[0].adminstrationType").value("업무"));
    }
    @Test
    public void testFindById_Success() throws Exception {
        String administrationId = "AFDSFBSFDSadsdds1459";
        AdministrationDTO administrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();
        // 세션이 있다고 가정
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 서비스가 DTO 반환하도록 모킹
        Mockito.when(administrationService.FindByAdministration(administrationId)).thenReturn(administrationmockDTO);

        mockMvc.perform(get("/api/time/search")
                        .param("AdministrationId", administrationId)
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.data[0].administrationId").value(administrationId));
    }

    @Test
    public void testFindAllWithPageDTO() throws Exception {
        // given

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class)))
                .thenAnswer(invocation -> {
                    HttpServletRequest request = invocation.getArgument(0);
                    Object userId = request.getSession().getAttribute("userId");
                    return userId != null;
                });

        AdministrationDTO administrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();

        List<AdministrationDTO> administrationDTOS = List.of(administrationmockDTO);


        // addressService.findAll(page) 호출 시 ResponseDTO 형태 반환
        Mockito.when(administrationService.FindAll())
                .thenReturn(administrationDTOS);

        // when & then
        mockMvc.perform(post("/api/time/search?page=0")
                        .sessionAttr("userId", "testUserId")  // 여기 세션에 userId 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].administrationId").value("AFDSFBSFDSadsdds1459"));
    }

    @Test
    public void testDelete() throws Exception {
        String administrationId = "abc123";

        // 세션 존재 여부 모킹
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class)))
                .thenReturn(true);

        Mockito.when(administrationService.Delete(administrationId))
                .thenReturn(true); // 혹은 필요한 성공 값

        mockMvc.perform(delete("/api/time")
                        .param("administrationId", administrationId)
                        .sessionAttr("userId", "testUserId") // 세션 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("일정 데이터 삭제에 성공하셨습니다."));
        //컨트롤러와 목업의 반환 값을 일치 시켜 줘야 한다.
    }


    @Test
    public void testUpdate_Success() throws Exception {
        AdministrationDTO InsertadministrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();

        // 기타 필드도 세팅

        AdministrationDTO UpdateadministrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,27))
                .adminstrationType(DateType.업무)
                .build();
        // update 결과로 반환될 필드 세팅

        // 세션 존재 모킹
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 서비스 update 호출 모킹
        Mockito.when(administrationService.Update(Mockito.any(AdministrationDTO.class))).thenReturn(UpdateadministrationmockDTO);

        mockMvc.perform(put("/api/time")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(InsertadministrationmockDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("일정 데이터 업데이트에 성공하셨습니다."))
                .andExpect(jsonPath("$.data[0].administrationDate").value("2025-06-27"))
                .andExpect(jsonPath("$.data[0].adminstrationType").value("업무"));
    }

    @Test
    public void testUpdate_SessionNotExist() throws Exception {
        AdministrationDTO InsertadministrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();

        // 기타 필드도 세팅

        // 세션이 없다고 가정
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(false);

        mockMvc.perform(put("/api/time")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(InsertadministrationmockDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value("error"))
                .andExpect(jsonPath("$.message").value("데이터 베이스의 데이터를 수정하는 과정에서 예기치 못한 오류가 발생했습니다."));

        // update()는 아예 호출되지 않아야 함
//        Mockito.verify(addressService, Mockito.never()).update(Mockito.any(AddressDTO.class));
    }

    @Test
    public void testUpdate_FailedUpdate() throws Exception {
        AdministrationDTO InsertadministrationmockDTO = AdministrationDTO.builder()
                .administrationId("AFDSFBSFDSadsdds1459")
                .administrationDate(LocalDate.of(2025,06,25))
                .adminstrationType(DateType.업무)
                .build();

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 업데이트 실패 시 null 반환하도록 모킹
        Mockito.when(administrationService.Update(Mockito.any(AdministrationDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/time")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(InsertadministrationmockDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value("error"))
                .andExpect(jsonPath("$.message").value("데이터 베이스의 데이터를 수정하는 과정에서 예기치 못한 오류가 발생했습니다."));
    }
}
