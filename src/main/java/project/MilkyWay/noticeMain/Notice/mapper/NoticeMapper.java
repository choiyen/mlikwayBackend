package project.MilkyWay.noticeMain.Notice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.Reservation.Entity.ReservationEntity;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;

import java.util.List;

@Mapper
public interface NoticeMapper
{
    List<NoticeEntity> findAll();

    List<NoticeEntity> findByType(
            @Param("type") String type,
            @Param("offset") long offset,
            @Param("limit") Integer limit
    );
    NoticeEntity findByNoticeId(String NoticeId);
    void deleteByNoticeId(String NoticeId);
    List<NoticeEntity> findAll2(long offset, Integer limit);
    Long totalRecord();



    void Insert(NoticeEntity noticeEntity);
    void Update(NoticeEntity noticeEntity);

}
