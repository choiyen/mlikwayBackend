package project.MilkyWay.Administration.Controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.MilkyWay.Address.DTO.AddressDTO;
import project.MilkyWay.Administration.DTO.AdministrationDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.Administration.Entity.AdministrationEntity;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.ComonType.Expection.SessionNotFoundExpection;
import project.MilkyWay.ComonType.LoginSuccess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/time")
@Tag(name = "일정 관련 정보를 제공하는  Controller")
public class AdministrationController
{
    @Autowired
    AdministrationService administrationService;

    ResponseDTO<AdministrationDTO> responseDTO = new ResponseDTO<>();

    LoginSuccess loginSuccess = new LoginSuccess();

    @Operation(
            summary = "Create a new Administration , but only if the user is an administrator. ",
            description = "This API creates a new Administration and returns AdministrationDTO as response",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Administration created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdministrationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<?> Insert(HttpServletRequest request, @Valid @RequestBody AdministrationDTO administrationDTO)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                if(administrationDTO.getAdminstrationType().equals("일하는날") == true)
                {
                    throw new InsertFailedException("고객 정보의 추가 없이 일하는 날 정보를 추가하지 못해요");
                }
                else
                {
                    if(administrationService.exists(administrationDTO.getAdministrationId()) || administrationService.existsByDate(administrationDTO.getAdministrationDate()))
                    {
                        throw new InsertFailedException("해당 코드나 날짜를 가진 일정이 이미 있어서, 추가가 불가능 합니다.");
                    }
                    else
                    {
                        String uniqueId;
                        do
                        {
                            uniqueId = loginSuccess.generateRandomId(15);
                            boolean bool = administrationService.FindByAdministrationBool(uniqueId);
                            if(!bool)
                            {
                                break;
                            }
                        }while (true);
                        AdministrationEntity administration = ConvertToEntity(administrationDTO, uniqueId);
                        AdministrationEntity administrationEntity = administrationService.insert(administration);
                        if(administrationEntity != null)
                        {
                            AdministrationDTO administrationDTO1 = ConvertToDTO(administrationEntity);
                            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success","일정 데이터 추가에 성공하셨습니다.", Collections.singletonList(administrationDTO1)));
                        }
                        else
                        {
                            throw new InsertFailedException("데이터 베이스에 데이터를 추가하는 과정에서 예기치 못한 오류가 발생했습니다.");
                        }
                    }
                }

            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 사내 일정 조회 불가");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }//에러 메세지가 정상적으로 송출되지 않음을 확인 - 코드 수정 필요


    @Operation(
            summary = "Change a Administration by AdministrationId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API changes an Administration and returns AdministrationDTO as response",  // Provide detailed description
            responses = {
                    @ApiResponse(responseCode = "201", description = "Administration Changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdministrationDTO.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid Change data"
                    )
            }
    )
    @PutMapping
    public ResponseEntity<?> Update(HttpServletRequest request, @Valid @RequestBody AdministrationDTO administrationDTO)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AdministrationEntity administration = ConvertToEntity(administrationDTO);
                AdministrationEntity administrationEntity = administrationService.Update(administration);
                if(administrationEntity != null)
                {
                    AdministrationDTO administrationDTO1 = ConvertToDTO(administrationEntity);
                    return ResponseEntity.ok().body(responseDTO.Response("success","일정 데이터 업데이트에 성공하셨습니다.", Collections.singletonList(administrationDTO1)));
                }
                else
                {
                    throw new InsertFailedException("데이터 베이스의 데이터를 수정하는 과정에서 예기치 못한 오류가 발생했습니다.");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 회사 일정 변경 불가");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Delete an administration by administrationId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API deletes an administration by the provided administrationId and returns a ResponseEntity with a success or failure message",  // Provide detailed description
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Administration deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Administration not found"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<?> Delete(HttpServletRequest request, @RequestParam String administrationId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                boolean bool = administrationService.Delete(administrationId);
                if(bool)
                {
                    return ResponseEntity.ok().body(responseDTO.Response("success","일정 데이터 삭제에 성공하셨습니다."));
                }
                else
                {
                    throw new InsertFailedException("데이터 베이스에 데이터를 삭제하는 과정에서 예기치 못한 오류가 발생했습니다.");

                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 일정 정보 삭제를 위해선 관리자 로그인이 필요해요");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @Operation(
            summary = "Returns AdministrationDTO object for a given Address Id , but only if the user is an administrator." ,
            description = "This API retrieves an Administration based on the provided Administration Id and returns the corresponding AdministrationDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Address found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdministrationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Address not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> FindAdministration(HttpServletRequest request, @RequestParam String AdministrationId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AdministrationEntity administration = administrationService.FindByAdministration(AdministrationId);
                if(administration != null)
                {
                    AdministrationDTO administrationDTO = ConvertToDTO(administration);
                    return ResponseEntity.ok().body(responseDTO.Response("success","일정 데이터 찾기 성공", Collections.singletonList(administrationDTO)));
                }
                else
                {
                    throw new FindFailedException("예기치 못한 오류로 일정 데이터 찾기를 보류합니다. 에러코드를 확인해주세요");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 개발 조회를 위해선 관리자 로그인이 필요해요");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Returns a list of administrationDTO objects",
            description = "This API retrieves a list of administrationDTO objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "administration List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
                    @ApiResponse(responseCode = "404", description = "administration List not found")
            }
    )
    @PostMapping({"/search/{page}", "/search"})
    public ResponseEntity<?> FindAll(@PathVariable(value = "page",required = false) LocalDate Date)
    {
        try
        {

            if(Date == null)
            {
                List<AdministrationEntity> administrationEntities = administrationService.FindAll();
                if(administrationEntities.isEmpty())
                {
                    return ResponseEntity.ok().body(responseDTO.Response("empty", "데이터를 조사하였으나, 작성된 예약 일정 없음"));
                }
                List<AdministrationDTO> administrationDTOS = new ArrayList<>();
                for (AdministrationEntity administrationEntity : administrationEntities)
                {
                    administrationDTOS.add(ConvertToDTO(administrationEntity));
                }
                return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에서 일정데이터 전체조회 성공",  administrationDTOS));
            }
            else
            {
                List<AdministrationEntity> administrationEntities = administrationService.FindByAdministrationDateBetween(Date);
                List<AdministrationDTO> administrationDTOS = new ArrayList<>();
                for (AdministrationEntity administrationEntity : administrationEntities)
                {
                    administrationDTOS.add(ConvertToDTO(administrationEntity));
                }
                return ResponseEntity.ok().body(responseDTO.Response("success","데이터베이스에서 일정데이터 전체조회 성공",  administrationDTOS));
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    } //일정이 없는 날짜에만 고객이 회사의 전체 등록을 알아야 할 테니까, 일단 보류 -> 따로 일정만 가져오는 함수를 만들어야 할 려나?

    private AdministrationDTO ConvertToDTO(AdministrationEntity administrationEntity)
    {
        return AdministrationDTO.builder()
                .administrationId(administrationEntity.getAdministrationId())
                .administrationDate(administrationEntity.getAdministrationDate())
                .adminstrationType(administrationEntity.getAdminstrationType())
                .build();
    }

    private AdministrationEntity ConvertToEntity(AdministrationDTO administrationDTO)
    {
        return AdministrationEntity.builder()
                .administrationId(administrationDTO.getAdministrationId())
                .adminstrationType(administrationDTO.getAdminstrationType())
                .administrationDate(administrationDTO.getAdministrationDate())
                .build();
    }
    private AdministrationEntity ConvertToEntity(AdministrationDTO administrationDTO, String uniqueId)
    {
        return AdministrationEntity.builder()
                .administrationId(uniqueId)
                .adminstrationType(administrationDTO.getAdminstrationType())
                .administrationDate(administrationDTO.getAdministrationDate())
                .build();
    }

}
