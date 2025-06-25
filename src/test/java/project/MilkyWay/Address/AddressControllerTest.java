package project.MilkyWay.Address;

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
import project.MilkyWay.Address.Controller.AddressController;
import project.MilkyWay.Address.DTO.AddressDTO;
import project.MilkyWay.Address.Repository.AddressRepository;
import project.MilkyWay.Address.Service.AddressService;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AddressController.class)
@Import(TestSecurityConfig.class)  // ✅ 테스트용 보안 설정 추가
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private AdministrationService administrationService;

    ResponseDTO responseDTO = new ResponseDTO();

    @MockBean
    private AddressRepository addressRepository;

    @MockBean
    private LoginSuccess loginSuccess;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());



    @Test
    public void testCreateAddress() throws Exception {
        // given (테스트용 mock DTO, Entity 생성)
        AddressDTO mockDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("경상남도 진주시 아리수로 13-1 글랜트빌 101호")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025,6,25))
                .acreage("30평")
                .cleanType(CleanType.이사청소)
                .build();

        // Service.create() 호출 시 mockDTO 반환하도록 설정
        Mockito.when(addressService.insert(any(AddressDTO.class)))
                .thenReturn(mockDTO);

        // 요청 JSON (String)
        String requestJson = """
            {
                "addressId": "abc123",
                "customer": "홍길동",
                "address": "경상남도 진주시 아리수로 13-1 글랜트빌 101호",
                "phoneNumber": "010-1234-5678",
                "submissionDate": "2025-06-25",
                "acreage": "30평",
                "cleanType": "이사청소"
            }
            """;

        // when - API 호출 및 then - 검증
        mockMvc.perform(post("/api/address")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].customer").value("홍길동"))
                .andExpect(jsonPath("$.data[0].cleanType").value("이사청소"));
    }


    @Test
    public void testFindById_Success() throws Exception {
        String addressId = "abc123";
        AddressDTO mockDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("경상남도 진주시 아리수로 13-1 글랜트빌 101호")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025,6,25))
                .acreage("30평")
                .cleanType(CleanType.이사청소)
                .build();
        // 세션이 있다고 가정
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 서비스가 DTO 반환하도록 모킹
        Mockito.when(addressService.findByAddressId(addressId)).thenReturn(mockDTO);

        mockMvc.perform(post("/api/address/search")
                        .param("AddressId", addressId)
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.data[0].addressId").value(addressId));
    }

    @Test
    public void testFindByDate_Success() throws Exception {
        LocalDate date = LocalDate.of(2025, 6, 25);
        AddressDTO mockDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("경상남도 진주시 아리수로 13-1 글랜트빌 101호")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025,6,25))
                .acreage("30평")
                .cleanType(CleanType.이사청소)
                .build();

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);
        Mockito.when(addressService.FindBySubmissionDate(date)).thenReturn(mockDTO);

        mockMvc.perform(get("/api/address/search/Date")
                        .param("AdminstrationDate", date.toString())
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.data[0].submissionDate").value(date.toString()));
    }


    @Test
    public void testDelete() throws Exception {
        String addressId = "abc123";

        // 세션 존재 여부 모킹
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class)))
                .thenReturn(true);

        // 서비스 Delete 호출 모킹 (성공 시 true 반환)
        Mockito.when(addressService.Delete(addressId)).thenReturn(true);

        Mockito.when(administrationService.Delete(addressId))
                .thenReturn(true); // 혹은 필요한 성공 값

        mockMvc.perform(delete("/api/address")
                        .param("addressId", addressId)
                        .sessionAttr("userId", "testUserId") // 세션 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("데이터베이스에 주소 데이터 삭제 성공"));
        //컨트롤러와 목업의 반환 값을 일치 시켜 줘야 한다.
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

        AddressDTO dto = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("서울시 강남구")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025, 6, 25))
                .acreage("20평")
                .cleanType(CleanType.이사청소)
                .build();

        List<AddressDTO> addressList = List.of(dto);
        List<Object> objList = new ArrayList<>(addressList);
        PageDTO pageDTO = PageDTO.builder()
                .Total(100L)
                .PageCount(5)
                .list(objList)
                .build();


        // addressService.findAll(page) 호출 시 ResponseDTO 형태 반환
        Mockito.when(addressService.findALL(anyInt()))
                .thenReturn(pageDTO);

        // when & then
        mockMvc.perform(get("/api/address/search?page=0")
                        .sessionAttr("userId", "testUserId")  // 여기 세션에 userId 넣기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageDTO.list[0].customer").value("홍길동"))
                .andExpect(jsonPath("$.pageDTO.pageCount").value(5))
                .andExpect(jsonPath("$.pageDTO.total").value(100));
    }

    @Test
    public void testUpdate_Success() throws Exception {
        AddressDTO inputDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("서울시 강남구")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025, 6, 25))
                .acreage("20평")
                .cleanType(CleanType.이사청소)
                .build();

        // 기타 필드도 세팅

        AddressDTO UpdateDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("서울시 강남구")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025, 6, 25))
                .acreage("20평")
                .cleanType(CleanType.이사청소)
                .build();
        // update 결과로 반환될 필드 세팅

        // 세션 존재 모킹
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 서비스 update 호출 모킹
        Mockito.when(addressService.update(Mockito.any(AddressDTO.class))).thenReturn(UpdateDTO);

        mockMvc.perform(put("/api/address")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("success"))
                .andExpect(jsonPath("$.message").value("데이터베이스에 주소 데이터 수정"))
                .andExpect(jsonPath("$.data[0].addressId").value("abc123"))
                .andExpect(jsonPath("$.data[0].customer").value("홍길동"));
    }

    @Test
    public void testUpdate_SessionNotExist() throws Exception {
        AddressDTO inputDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("서울시 강남구")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025, 6, 25))
                .acreage("20평")
                .cleanType(CleanType.이사청소)
                .build();

        // 세션이 없다고 가정
        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(false);

        mockMvc.perform(put("/api/address")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value("error"))
                .andExpect(jsonPath("$.message").value("청소 예약 정보 등록 과정에서 오류 발생!! 관리자에게 문의하세요"));

        // update()는 아예 호출되지 않아야 함
//        Mockito.verify(addressService, Mockito.never()).update(Mockito.any(AddressDTO.class));
    }

    @Test
    public void testUpdate_FailedUpdate() throws Exception {
        AddressDTO inputDTO = AddressDTO.builder()
                .addressId("abc123")
                .customer("홍길동")
                .address("서울시 강남구")
                .phoneNumber("010-1234-5678")
                .submissionDate(LocalDate.of(2025, 6, 25))
                .acreage("20평")
                .cleanType(CleanType.이사청소)
                .build();

        Mockito.when(loginSuccess.isSessionExist(Mockito.any(HttpServletRequest.class))).thenReturn(true);

        // 업데이트 실패 시 null 반환하도록 모킹
        Mockito.when(addressService.update(Mockito.any(AddressDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/address")
                        .sessionAttr("userId", "testUserId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value("error"))
                .andExpect(jsonPath("$.message").value("청소 예약 정보 등록 과정에서 오류 발생!! 관리자에게 문의하세요"));
    }

}

