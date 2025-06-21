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
import project.MilkyWay.Administration.DTO.AdministrationDTO;
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
@RequestMapping("/api/address")
public class AddressController {
    @Autowired
    AddressService addressService;
// 음....
    @Autowired
    AdministrationService administrationService;


    ResponseDTO<AddressDTO> responseDTO = new ResponseDTO<>();

    LoginSuccess loginSuccess = new LoginSuccess();



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

                AdministrationEntity administration = administrationService.FindByAdministrationDate(addressDTO.getSubmissionDate());

                if(administration == null)
                {

                    AddressDTO addressDTO1 = addressService.insert(addressDTO);
                    if(addressDTO1 != null)
                    {
                        AdministrationDTO administrationDTO = AdministrationDTO.builder()
                                .administrationId(addressDTO1.getAddressId())
                                .adminstrationType(DateType.업무)
                                .administrationDate(addressDTO.getSubmissionDate())
                                .build();
                        administrationService.insert(administrationDTO, addressDTO1.getAddressId());
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

                    AddressDTO addressDTO1 = addressService.insert(addressDTO);
                    if(addressDTO1 != null)
                    {
                        AdministrationDTO administrationDTO = AdministrationDTO.builder()
                                .administrationId(addressDTO1.getAddressId())
                                .adminstrationType(DateType.업무)
                                .administrationDate(addressDTO.getSubmissionDate())
                                .build();
                        administrationService.insert(administrationDTO, addressDTO1.getAddressId());
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
                AddressDTO addressDTO1 = addressService.update(addressDTO);
                if(addressDTO1 != null)
                {
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

                PageDTO pageDTO = addressService.findALL(page);
                if(pageDTO.getList().isEmpty())
                {
                    return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에 내용은 비어있음"));
                }
                else {

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
                AddressDTO addressDTO = addressService.findByAddressId(AddressId);
                if(addressDTO != null)
                {
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
                AddressDTO addressDTO = addressService.FindBySubmissionDate(AdminstrationDate);
                if(addressDTO != null)
                {
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

}
//- 현재 날짜보다 고객의 의뢰 날짜가 뒷날일 떄 데이터를 파기하는 함수 필요
//고객 관리를 위한 목적의 DTO