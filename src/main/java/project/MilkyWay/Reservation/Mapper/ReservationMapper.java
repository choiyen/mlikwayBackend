package project.MilkyWay.Reservation.Mapper;

import org.apache.ibatis.annotations.Mapper;
import project.MilkyWay.Reservation.Entity.ReservationEntity;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReservationMapper
{

    List<ReservationEntity> findAll(Integer offset, Integer limit);
    ReservationEntity findByAdministrationId(String administrationId);
    ReservationEntity findByReservationId(String reservationId);
    void deleteByReservationId(String reservationId);
    void Insert(ReservationEntity reservationEntity);
    void Update(ReservationEntity reservationEntity);
    Long totalRecord();

}
