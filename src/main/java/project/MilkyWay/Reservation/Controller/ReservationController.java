package project.MilkyWay.Reservation.Controller;


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
import project.MilkyWay.Administration.Entity.AdministrationEntity;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Expection.*;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Reservation.Entity.ReservationEntity;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.Reservation.DTO.ReservationDTO;
import project.MilkyWay.Reservation.Service.ReservationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/reserve")
@Tag(name = "reservation 정보를 제공하는 Controller")
public class ReservationController //고객의 예약을 관리하기 위한 DTO
{
    @Autowired
    ReservationService reservationService;

    ResponseDTO<ReservationDTO> responseDTO = new ResponseDTO<>();

    @Autowired
    AdministrationService administrationService;

    LoginSuccess loginSuccess = new LoginSuccess();

    @Operation(
            summary = "Create a new reservation",  // Provide a brief summary
            description = "This API creates a new reservation and returns reservationDTO as response",  // Provide detailed description
            responses = {
                    @ApiResponse(responseCode = "201", description = "reservation created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<?> Insert(@RequestBody @Valid ReservationDTO reservationDTO) {
        try
        {



            String uniqueId;
            LoginSuccess loginSuccess = new LoginSuccess();
            do
            {
                uniqueId = loginSuccess.generateRandomId(15);
                ReservationEntity reservationEntity = reservationService.SelectAdminstrationID(uniqueId);
                if(reservationEntity == null)
                {
                    break;
                }
            }while (true);

                ReservationEntity reservationEntity = ConvertToEntity(reservationDTO, uniqueId);
                ReservationEntity reservationEntity2 = reservationService.InsertReservation(reservationEntity);
                if (reservationEntity2 != null)
                {
                    ReservationDTO reservationDTO1 = ConvertToDTO(reservationEntity2);
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success", "데이터 추가에 성공하였습니다.", Collections.singletonList(reservationDTO1)));
                }
                else
                {
                    throw new InsertFailedException();
                }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }//테스트 완료 : 단, 사용자가 예약을 하고, 예약정보를 확인한 후, 승인이 되었을 떄, 일정 정보에 추가하는 형태로 바꿀 지는 고민 필요.


    @Operation(
            summary = "Change a ReservationDTO by ReservationId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API Change a Reservation and returns ReservationDTO as response",  // Provide detailed description
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reservation Changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid Change data"
                    )
            }
    )
    @PutMapping
    public ResponseEntity<?> Update(HttpServletRequest request, @RequestBody @Valid ReservationDTO reservationDTO) {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                ReservationEntity reservationEntity = ConvertToEntity(reservationDTO);
                ReservationEntity reservationEntity2 = reservationService.SaveReservation(reservationEntity);
                if (reservationEntity2 != null) {
                    ReservationDTO reservationDTO1 = ConvertToDTO(reservationEntity2);
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 수정에 성공하였습니다.", Collections.singletonList(reservationDTO1)));
                }
                else
                {
                    throw new UpdateFailedException("고객 예약 정보 업데이트 도중 오류 발생, 관리자에게 문의하세요");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 고객의 예약 정보 수정은 관리자의 몫입니다.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Delete an Reservation by ReservationId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API deletes an Reservation by the provided ReservationId and returns a ResponseEntity with a success or failure message.",  // Provide detailed description
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reservation deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reservation not found"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<?> Delete(HttpServletRequest request, @RequestParam String ReservationId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                boolean bool = reservationService.DeleteReservation(ReservationId);
                if (bool)
                {
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 삭제에 성공하였습니다."));
                }
                else
                {
                    throw new DeleteFailedException();
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 고객 예약 정보 삭제는 관리자 로그인이 필요합니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @Operation(
            summary = "Returns a list of ReservationDTO objects, but only if the user is an administrator.",
            description = "This API retrieves a list of ReservationDTO objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reservation List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Reservation List not found")
            }
    )
    @GetMapping
    public ResponseEntity<?> FindAll(HttpServletRequest request, @RequestParam(name = "page", defaultValue = "0") Integer page)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {

                List<ReservationEntity> reservationEntities = reservationService.ListReservation(page);
                List<ReservationDTO> reservationDTOS = new ArrayList<>();
                for (ReservationEntity reservationEntity : reservationEntities) {
                    reservationDTOS.add(ConvertToDTO(reservationEntity));
                }
                if (reservationDTOS.isEmpty())
                {
                    return ResponseEntity.ok().body(responseDTO.Response("empty","데이터베이스에 내용은 비어있음"));
                }
                else
                {
                    PageDTO pageDTO = PageDTO.<ReservationDTO>builder()
                            .list(reservationDTOS)
                            .PageCount(reservationService.totalPaging())
                            .Total(reservationService.totalRecord())
                            .build();
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 조회에 성공했습니다.", pageDTO));
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 고객 정보의 확인은 관리자의 로그인을 필요로 합니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @Operation(
            summary = "Returns ReservationDTO object for a given ReservationId ",
            description = "This API retrieves an Reservation based on the provided Reservation Id and returns the corresponding ReservationDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ReservationDTO found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "ReservationDTO not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> FindBy(HttpServletRequest request, @RequestParam String ReservationId)
    {

        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                ReservationEntity reservationEntity = reservationService.ReservationSelect(ReservationId);
                if (reservationEntity == null) {
                    throw new FindFailedException("결과를 찾을 수 없습니다.");
                } else {
                    ReservationDTO reservationDTO = ConvertToDTO(reservationEntity);
                    return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료", Collections.singletonList(reservationDTO)));
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 고객 정보는 관리자 로그인이 없으면 확인하지 못하도록 보호되어 있습니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Returns ReservationDTO object for a given AdminstrationId ",
            description = "This API retrieves an Reservation based on the provided AdminstrationId and returns the corresponding ReservationDTO object.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ReservationDTO found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "ReservationDTO not found")
            }
    )
    @GetMapping("/search/admin")
    ResponseEntity<?> FindByAdmin(HttpServletRequest request, @RequestParam LocalDate AdminstrationDate)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                AdministrationEntity administrationEntity = administrationService.FindByAdministrationDate(AdminstrationDate);
                if(administrationEntity != null)
                {
                    ReservationEntity reservationEntity = reservationService.SelectAdminstrationID(administrationEntity.getAdministrationId());
                    if (reservationEntity == null)
                    {
                        return ResponseEntity.ok().body(responseDTO.Response("empty", "결과 값이 비어 있습니다."));
                    }
                    else
                    {
                        ReservationDTO reservationDTO = ConvertToDTO(reservationEntity);
                        return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료", Collections.singletonList(reservationDTO)));
                    }
                }
                else
                {
                    throw new  FindFailedException("해당 날짜의 일정을 못찾겠어요. 서버 관리자에게 문의 바람");
                }

            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 로그인 정보가 없는 상태에서 고객 예약 정보는 확인 불가합니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
    private ReservationDTO ConvertToDTO(ReservationEntity reservationEntity2)
    {
        return ReservationDTO.builder()
                .reservationId(reservationEntity2.getReservationId())
                .administrationId(reservationEntity2.getReservationId())
                .phone(reservationEntity2.getPhone())
                .address(reservationEntity2.getAddress())
                .name(reservationEntity2.getName())
                .acreage(reservationEntity2.getAcreage())
                .subissionDate(reservationEntity2.getSubissionDate())
                .type(reservationEntity2.getType())
                .build();
    }

    private ReservationEntity ConvertToEntity(ReservationDTO reservationDTO)
    {
        return ReservationEntity.builder()
                .reservationId(reservationDTO.getReservationId())
                .administrationId(reservationDTO.getAdministrationId())
                .phone(reservationDTO.getPhone())
                .address(reservationDTO.getAddress())
                .name(reservationDTO.getName())
                .acreage(reservationDTO.getAcreage())
                .subissionDate(reservationDTO.getSubissionDate())
                .type(reservationDTO.getType())
                .build();
    }
    private ReservationEntity ConvertToEntity(ReservationDTO reservationDTO, String uniqueId) {
        return ReservationEntity.builder()
                .reservationId(uniqueId)
                .administrationId(uniqueId)
                .phone(reservationDTO.getPhone())
                .address(reservationDTO.getAddress())
                .name(reservationDTO.getName())
                .acreage(reservationDTO.getAcreage())
                .subissionDate(reservationDTO.getSubissionDate())
                .type(reservationDTO.getType())
                .build();
    }
}
