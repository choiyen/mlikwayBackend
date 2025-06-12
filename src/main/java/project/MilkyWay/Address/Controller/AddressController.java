package project.MilkyWay.Address.Controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import project.MilkyWay.Address.DTO.AddressDTO;
import project.MilkyWay.Administration.Entity.AdministrationEntity;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.BoardMain.Board.DTO.BoardDTO;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.Address.Entity.AddressEntity;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.Address.Service.AddressService;
import project.MilkyWay.ComonType.Expection.SessionNotFoundExpection;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.ComonType.LoginSuccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Tag(name = "주소 관련 정보를 제공하는  Controller")
@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    AddressService addressService;

    @Autowired
    AdministrationService administrationService;


    ResponseDTO<AddressDTO> responseDTO = new ResponseDTO<>();

    LoginSuccess loginSuccess = new LoginSuccess();

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void scheduledDeleteOldSubmissions() {
        boolean deleted = addressService.deleteSubmissionBeforeTodayAtSixAM();
        boolean deletedadmini = administrationService.deleteByAdministrationDateBeforeTodayAtSixAM();
        if (deleted && deletedadmini) {
            System.out.println("[삭제 완료] 이전 날짜의 데이터가 성공적으로 삭제되었습니다.");
        } else {
            System.out.println("[삭제 미수행] 현재 시간이 오전 6시 이후입니다.");
        }
    }

    @PostConstruct
    public void init() {
        boolean deleted =addressService.deleteSubmissionBeforeTodayAtSixAM();
        boolean deletedadmini = administrationService.deleteByAdministrationDateBeforeTodayAtSixAM();
        if (deleted && deletedadmini) {
            System.out.println("delete check");
        } else {
            System.out.println("delete uncheck");
        }
    }

    @Operation(
            summary = "Create a new Address",
            description = "This API creates a new Address and returns AddressDTO as response, but only if the user is an administrator.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Address created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<?> Insert(HttpServletRequest request, @Valid @RequestBody AddressDTO addressDTO)
    {
        try
        {

            if(loginSuccess.isSessionExist(request))
            {
                String uniqueId;
                do
                {
                    uniqueId = loginSuccess.generateRandomId(15);
                    AddressEntity addressEntity = addressService.findByAddressId(uniqueId);
                    Boolean bool = administrationService.FindByAdministrationBool(uniqueId);
                    if(addressEntity == null && !bool)
                    {
                        break;
                    }
                }while (true);

                AdministrationEntity administration = administrationService.FindByAdministrationDate(addressDTO.getSubmissionDate());

                if(administration == null)
                {
                    AddressEntity addressEntity = ConvertToEntity(addressDTO,uniqueId);
                    AddressEntity addressEntity1 = addressService.insert(addressEntity);
                    if(addressEntity1 != null)
                    {
                        AdministrationEntity administrationEntity = AdministrationEntity.builder()
                                .administrationId(uniqueId)
                                .adminstrationType(DateType.업무)
                                .administrationDate(addressDTO.getSubmissionDate())
                                .build();
                        administrationService.insert(administrationEntity);
                        AddressDTO addressDTO1 = ConvertToDTO(addressEntity1);
                        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success","데이터베이스에 주소 데이터 추가", Collections.singletonList(addressDTO1)));
                    }
                    else
                    {
                        throw new RuntimeException("예기치 못한 오류로 런타임 오류 발생!!");
                    }
                }
                else
                {
                    switch (administration.getAdminstrationType()) {
                        case 업무, 연가 -> {
                            throw new FindFailedException(
                                    administration.getAdminstrationType() == DateType.업무
                                            ? "이미 일하는 일정이라 추가할 수 없어요. 일정부터 지워주세요."
                                            : "중요한 일이 있어서 꼭 쉬어야 하는 날이에요. 잘못 설정했다면 일정부터 지워주세요."
                            );
                        }
                        case 휴일, 예약 -> {
                            administrationService.Delete(administration.getAdministrationId());
                            // 이후 처리 로직은 switch 바깥에서 계속 진행
                        }
                    }

                    AddressEntity addressEntity = ConvertToEntity(addressDTO,uniqueId);
                    AddressEntity addressEntity1 = addressService.insert(addressEntity);
                    if(addressEntity1 != null)
                    {
                        AdministrationEntity administrationEntity = AdministrationEntity.builder()
                                .administrationId(uniqueId)
                                .adminstrationType(DateType.업무)
                                .administrationDate(addressDTO.getSubmissionDate())
                                .build();
                        administrationService.insert(administrationEntity);
                        AddressDTO addressDTO1 = ConvertToDTO(addressEntity1);
                        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success","데이터베이스에 주소 데이터 추가", Collections.singletonList(addressDTO1)));
                    }
                    else
                    {
                        throw new RuntimeException("예기치 못한 오류로 런타임 오류 발생!!");
                    }
                }

            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 주소 정보를 추가할 수 없습니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }

    @Operation(
            summary = "Change an Address by AddressId",
            description = "This API changes an Address and returns AddressDTO as response, but only if the user is an administrator.",
            responses = {
                    @
                    ApiResponse(responseCode = "200", description = "Address Changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid Change data")
            }
    )
    @PutMapping
    public ResponseEntity<?> Update(HttpServletRequest request, @Valid @RequestBody AddressDTO addressDTO)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AddressEntity addressEntity = ConvertToEntity(addressDTO);
                AddressEntity addressEntity1 = addressService.update(addressEntity);
                if(addressEntity1 != null)
                {
                    AddressDTO addressDTO1 = ConvertToDTO(addressEntity1);
                    return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에 주소 데이터 수정", Collections.singletonList(addressDTO1)));
                }
                else
                {
                    throw new UpdateFailedException("청소 예약 정보 등록 과정에서 오류 발생!! 관리자에게 문의하세요");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인X, 다시 로그인을 시도하여 주세요");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));

        }
    }

    @Operation(
            summary = "Delete an Address by AddressId , but only if the user is an administrator.",
            description = "This API deletes an Address by the provided AddressId and returns a ResponseEntity with a success or failure message.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Address deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Address not found")
            }
    )
    @DeleteMapping
    public ResponseEntity<?> Delete(HttpServletRequest request,@RequestParam(name = "addressId") String addressId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                boolean bool = addressService.Delete(addressId);
                if(bool)
                {
                    boolean bool2 = administrationService.Delete(addressId);
                    if(bool2)
                    {
                        return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에 주소 데이터 삭제 성공"));
                    }
                    else
                    {
                        throw new DeleteFailedException("일정 삭제는 성공, 하지만 관리 테이블 삭제에 실패했습니다. 수동으로 삭제해주시고, 관리자에게 문의하세요.");
                    }
                }
                else
                {
                    throw new RuntimeException("예기치 못한 오류로 런타임 오류 발생!!");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 필요!! 권한이 없어서 데이터 삭제 불가");
            }
        } 
        catch (Exception e) 
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }


    @Operation(
            summary = "Returns a list of AddressDTO objects , but only if the user is an administrator.",
            description = "This API retrieves a list of AddressDTO objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Address List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Address List not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> FindAll(HttpServletRequest request, @RequestParam(name = "page", defaultValue = "0") Integer page)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {

                Page<AddressEntity> addressEntityList = addressService.findALL(page);
                if(addressEntityList.isEmpty())
                {
                    return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에 내용은 비어있음"));
                }
                else {
                    List<AddressDTO> addressDTOS = new ArrayList<>();
                    for (AddressEntity addressEntity : addressEntityList) {
                        addressDTOS.add(ConvertToDTO(addressEntity));
                    }
                    PageDTO pageDTO = PageDTO.<AddressDTO>builder().list(addressDTOS)
                            .PageCount(addressEntityList.getTotalPages())
                            .Total(addressEntityList.getTotalElements()).build();

                    return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에 주소 데이터 조회 성공",pageDTO));
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인X, 권한이 없어서 예약 정보 조회 불가");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }


    @Operation(
            summary = "Returns AddressDTO object for a given Address Id , but only if the user is an administrator.",
            description = "This API retrieves an Address based on the provided Address Id and returns the corresponding AddressDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Address found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Address not found")
            }
    )
    @PostMapping("/search")
    public ResponseEntity<?> FindById(HttpServletRequest request, @RequestParam String AddressId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AddressEntity addressEntity = addressService.findByAddressId(AddressId);
                if(addressEntity != null)
                {
                    AddressDTO addressDTO = ConvertToDTO(addressEntity);
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 조회 성공", Collections.singletonList(addressDTO)));
                }
                else
                {
                    throw new RuntimeException("예기치 못한 오류로 런타임 오류 발생!!");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 예약정보 조회 불가");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }
    @Operation(
            summary = "Returns AddressDTO object for a given Address Id , but only if the user is an administrator.",
            description = "This API retrieves an Address based on the provided Address Id and returns the corresponding AddressDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Address found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Address not found")
            }
    )
    @GetMapping("/search/Date")
    public ResponseEntity<?> FindByDate(HttpServletRequest request, @RequestParam LocalDate AdminstrationDate)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AddressEntity addressEntity = addressService.FindBySubmissionDate(AdminstrationDate);
                if(addressEntity != null)
                {
                    AddressDTO addressDTO = ConvertToDTO(addressEntity);
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 조회 성공", Collections.singletonList(addressDTO)));
                }
                else
                {
                    return ResponseEntity.ok().body(responseDTO.Response("empty", "데이터 조회 성공했으나 비어있음"));
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 예약정보 조회 불가");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }

    private AddressDTO ConvertToDTO(AddressEntity addressEntity1)
    {
        return AddressDTO.builder()
                .addressId(addressEntity1.getAddressId())
                .address(addressEntity1.getAddress())
                .customer(addressEntity1.getCustomer())
                .phoneNumber(addressEntity1.getPhoneNumber())
                .submissionDate(addressEntity1.getSubmissionDate())
                .acreage(addressEntity1.getAcreage())
                .cleanType(addressEntity1.getCleanType())
                .build();
    }

    private AddressEntity ConvertToEntity(AddressDTO addressDTO, String uniqueId)
    {
        return AddressEntity.builder()
                .addressId(uniqueId)
                .address(addressDTO.getAddress())
                .customer(addressDTO.getCustomer())
                .phoneNumber(addressDTO.getPhoneNumber())
                .submissionDate(addressDTO.getSubmissionDate())
                .acreage(addressDTO.getAcreage())
                .cleanType(addressDTO.getCleanType())
                .build();
    }
    private AddressEntity ConvertToEntity(AddressDTO addressDTO)
    {
        return AddressEntity.builder()
                .addressId(addressDTO.getAddressId())
                .address(addressDTO.getAddress())
                .customer(addressDTO.getCustomer())
                .phoneNumber(addressDTO.getPhoneNumber())
                .submissionDate(addressDTO.getSubmissionDate())
                .acreage(addressDTO.getAcreage())
                .cleanType(addressDTO.getCleanType())
                .build();
    }

}
//- 현재 날짜보다 고객의 의뢰 날짜가 뒷날일 떄 데이터를 파기하는 함수 필요
//고객 관리를 위한 목적의 DTO