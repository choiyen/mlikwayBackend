package project.MilkyWay.noticeMain.NoticeDetail.mapper;


import org.apache.ibatis.annotations.Mapper;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;


import java.util.List;


@Mapper
public interface NoticeDetailMapper
{
    List<NoticeDetailEntity> findByNoticeId(String NoticeId);
    NoticeDetailEntity findByNoticeDetailId(Long NoticeDetailId);
    void deleteByNoticeDetailId(Long NoticeDetailId);
    void Insert(NoticeDetailEntity noticeEntity);
    void Update(NoticeDetailEntity noticeEntity);

}
