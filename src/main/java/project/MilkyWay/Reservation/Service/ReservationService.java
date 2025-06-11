package project.MilkyWay.Reservation.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.MilkyWay.Administration.Entity.AdministrationEntity;
import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.Reservation.Entity.ReservationEntity;
import project.MilkyWay.ComonType.Enum.DateType;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Reservation.Mapper.ReservationMapper;


import java.util.List;


@Service
public class ReservationService //고객의 예약을 관리하기 위한 DTO
{
    @Autowired
    ReservationMapper reservationMapper;

    @Autowired
    AdministrationService administrationService;

    public ReservationEntity SelectAdminstrationID(String AdiminstrationId)
    {
        ReservationEntity reservationEntity = reservationMapper.findByAdministrationId(AdiminstrationId);
            return reservationEntity;

    }

    public ReservationEntity InsertReservation(ReservationEntity reservationEntity)
    {

        AdministrationEntity administration = administrationService.FindByAdministrationDate(reservationEntity.getSubissionDate());
        ReservationEntity reservationEntity1;
        if(administration!= null)
        {
            if(administration.getAdminstrationType() == DateType.업무 ||administration.getAdminstrationType() == DateType.연가)
            {
                throw new FindFailedException("다른 일정이 있어서 일정 추가가 반려되었습니다.");
            }
            else if(administration.getAdminstrationType() == DateType.예약)
            {
                throw new FindFailedException("이미 다른 사람이 예약한 날짜입니다. 다른 날짜를 선택해주세요");
            }
            else
            {
                if(!administration.getAdministrationDate().equals(reservationEntity.getSubissionDate()))
                {
                    throw new FindFailedException("관련 일정 데이터를 찾았으나, 일정 데이터의 날짜인 " + administration.getAdministrationDate() + "와 예약하려는 날짜 데이터의 날짜인 " + reservationEntity.getSubissionDate() + "가 서로 다른 것 같습니다. 확인해주세요");
                }
                else
                {
                    AdministrationEntity administrationEntity2 = AdministrationEntity.builder()
                            .administrationId(administration.getAdministrationId())
                            .adminstrationType(DateType.예약)
                            .administrationDate(administration.getAdministrationDate())
                            .build();
                    administrationService.Update(administrationEntity2);
                    reservationEntity1 = ReservationEntity.builder()
                            .type(reservationEntity.getType())
                            .reservationId(reservationEntity.getReservationId())
                            .acreage(reservationEntity.getAcreage())
                            .administrationId(administration.getAdministrationId())
                            .address(reservationEntity.getAddress())
                            .name(reservationEntity.getName())
                            .phone(reservationEntity.getPhone())
                            .subissionDate(reservationEntity.getSubissionDate())
                            .build();
                }
            }
        }
        else
        {
            AdministrationEntity administrationEntity = AdministrationEntity.builder()
                    .administrationId(reservationEntity.getAdministrationId())
                    .adminstrationType(DateType.예약)
                    .administrationDate(reservationEntity.getSubissionDate())
                    .build();
            administrationService.insert(administrationEntity);
            reservationEntity1 = ReservationEntity.builder()
                    .type(reservationEntity.getType())
                    .reservationId(reservationEntity.getReservationId())
                    .acreage(reservationEntity.getAcreage())
                    .administrationId(reservationEntity.getAdministrationId())
                    .address(reservationEntity.getAddress())
                    .name(reservationEntity.getName())
                    .phone(reservationEntity.getPhone())
                    .subissionDate(reservationEntity.getSubissionDate())
                    .build();

        }
        try
        {
            reservationMapper.Insert(reservationEntity1);
        }
        catch (Exception e)
        {
            administrationService.Delete(reservationEntity.getAdministrationId());
            throw new FindFailedException(e.getMessage() + reservationEntity.getType());
        }

        ReservationEntity reservationEntity2 = reservationMapper.findByReservationId(reservationEntity.getReservationId());
        if(reservationEntity2 != null)
        {
            return reservationEntity2;
        }
        else
        {
            throw new FindFailedException("해당 코드를 가진 예약 데이터가 존재하지 않습니다.");
        }
    }
    // Id가 아니라 날짜로 일정 데이터를 찾아야 함.


    public boolean DeleteReservation(String reservationId) {
        ReservationEntity reservationEntity1 = reservationMapper.findByReservationId(reservationId);
        if(reservationEntity1 != null)
        {
            reservationMapper.deleteByReservationId(reservationId);
            ReservationEntity reservationEntity2 = reservationMapper.findByReservationId(reservationId);
            if(reservationEntity2 == null)
            {
                administrationService.Delete(reservationEntity1.getAdministrationId());
                return true;
            }
            else
            {
                throw new DeleteFailedException("삭제를 시도했는데 아직 데이터가 살아있네요.");
            }
        }
        else
        {
            throw new FindFailedException("삭제하려고 데이터를 찾으려 했는데 없네요????");
        }

    }
    public ReservationEntity SaveReservation(ReservationEntity reservationEntity)
    {

            AdministrationEntity administrationEntity = administrationService.FindByAdministrationDate(reservationEntity.getSubissionDate());
            //Id에 해당하는 날짜는 기존 날짜를 가르킬 테니 뺴고, 변경할 날짜에 일정이 일하는 일정이 있는지 여부를 확인

            if(administrationEntity.getAdminstrationType() == DateType.업무)
            {
                throw new InsertFailedException("해당 날짜에는 일정이 존재하기에 변경할 수 없습니다.");
            }
            else
            {
                if(administrationEntity.getAdminstrationType() == DateType.휴일)
                {
                    throw new InsertFailedException("해당 날짜에는 일정이 존재하기에 변경할 수 없습니다.");
                }
                ReservationEntity Oldreservation = reservationMapper.findByReservationId(reservationEntity.getReservationId());
                if(Oldreservation != null)
                {
                    ReservationEntity ChangeReservation = ChangeReservation(Oldreservation, reservationEntity);
                    AdministrationEntity administrationEntity2 = AdministrationEntity
                            .builder()
                            .administrationId(Oldreservation.getAdministrationId())
                            .adminstrationType(DateType.업무)
                            .administrationDate(reservationEntity.getSubissionDate())
                            .build();
                    AdministrationEntity administration = administrationService.Update(administrationEntity2);
                    if(administration == null)
                    {
                        throw new UpdateFailedException("일정 정보 업데이트 도중 오류가 발생했습니다.");
                    }
                    else
                    {
                        reservationMapper.Update(ChangeReservation);
                        ReservationEntity reservationEntity2 = reservationMapper.findByReservationId(ChangeReservation.getReservationId());


                        boolean check = reservationEntity2.equals(ChangeReservation);
                        if(check)
                        {
                            return reservationEntity2;
                        }
                        else
                        {
                            throw new UpdateFailedException("예약 데이터 업데이트에 실패하였습니다. 관리자에게 문의해주세요");
                        }
                    }
                }
                else
                {
                    throw new UpdateFailedException("예약 데이터 찾기에 실패하였습니다. 관리자에게 문의해주세요.");
                }
            }
    }//기능 변경 고민을 제외하면 완료


    public ReservationEntity ReservationSelect(String reservationId)
    {
        return reservationMapper.findByReservationId(reservationId);
    }
    public List<ReservationEntity> ListReservation(int page)
    {
        return reservationMapper.findAll(page, 100);
    }
    public Long totalRecord() {
        return reservationMapper.totalRecord();
    }
    public Integer totalPaging() {
        Long totalRecords = totalRecord(); // 전체 레코드 수
        int pageSize = 100;               // 한 페이지에 보여줄 수

        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    private ReservationEntity ChangeReservation(ReservationEntity OldReservation, ReservationEntity newReservation)
    {
        return ReservationEntity.builder()
                .administrationId(OldReservation.getAdministrationId())
                .subissionDate(newReservation.getSubissionDate())
                .name(newReservation.getName())
                .acreage(newReservation.getAcreage())
                .address(newReservation.getAddress())
                .phone(newReservation.getPhone())
                .reservationId(OldReservation.getReservationId())
                .type(newReservation.getType())
                .build();
    }
}
