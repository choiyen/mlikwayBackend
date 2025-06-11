package project.MilkyWay.noticeMain.Notice.Service;


import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.noticeMain.Notice.mapper.NoticeMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService
{
   @Autowired
   NoticeMapper noticeMapper;

    public NoticeEntity InsertNotice(NoticeEntity noticeEntity)
   {
       noticeMapper.Insert(noticeEntity);
       NoticeEntity notice = noticeMapper.findByNoticeId(noticeEntity.getNoticeId());
       if(notice != null)
       {
           return notice;
       }
       else
       {
           throw new InsertFailedException("데이터베이스에 데이터를 추가 시키러고 시도했는데, 실패했나봐요ㅠㅠㅠ");
       }
   }
    public NoticeEntity UpdateNotice(String encodingNoticeId, NoticeEntity newNoticeEntity)
   {
       NoticeEntity OldNoticeEntity = noticeMapper.findByNoticeId(encodingNoticeId);
       if(OldNoticeEntity != null)
       {
           NoticeEntity newNoticeEntity2 = ChangeToNotice(OldNoticeEntity, newNoticeEntity);
           noticeMapper.Update(newNoticeEntity2);
           NoticeEntity SelectnewNoticeEntity = noticeMapper.findByNoticeId(encodingNoticeId);
           if(SelectnewNoticeEntity.getNoticeId().equals(newNoticeEntity2.getNoticeId()) && SelectnewNoticeEntity.getType().equals(newNoticeEntity2.getType())&&SelectnewNoticeEntity.getGreeting().equals(newNoticeEntity2.getGreeting()))
           {
               return SelectnewNoticeEntity;
           }
           else
           {
               throw new UpdateFailedException("데이터 수정을 시도할 수 있었는데, 수정엔 실패했네요. 관리자에게 문의하세요");
           }
       }
       else
       {
           throw new FindFailedException("해당 리뷰 정보에 해당하는 메인데이터를 못찾겠어요. 관리자에게 문의해줘요");
       }
   }
    public boolean DeleteByNoticeId(String encodingNoticeId) {
        NoticeEntity noticeEntity = noticeMapper.findByNoticeId(encodingNoticeId);
        if(noticeEntity != null)
        {
            noticeMapper.deleteByNoticeId(encodingNoticeId);
            NoticeEntity noticeEntity1 = noticeMapper.findByNoticeId(encodingNoticeId);
            if(noticeEntity1 == null)
            {
                return true;
            }
            else
            {
                throw new DeleteFailedException("삭제를 시도했는데 데이터가 살아있네요ㅠㅠㅠㅠ");
            }
        }
        else
        {
            throw new FindFailedException("데이터를 지울려고 했는데, 후기 Id에 맞는 정보가 없네요");
        }
    }
    public Long totalRecord() {
        return noticeMapper.totalRecord();
    }
    public Integer totalPaging() {
        Long totalRecords = totalRecord(); // 전체 레코드 수
        int pageSize = 100;               // 한 페이지에 보여줄 수

        return (int) Math.ceil((double) totalRecords / pageSize);
    }
    public List<NoticeEntity> findSmallAll(CleanType type, Long page)
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findByType(type.name(), page, 10));
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
        }

        if(list != null)
        {
            return list;
        }
        else
        {
            throw new FindFailedException("예기치 못한 오류가 발생하였습니다.");
        }
    }

    public List<NoticeEntity> findAll()
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findAll());
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
        }

        if(list.isEmpty() != true)
        {
            return list;
        }
        else if(list.isEmpty() == true)
        {
            throw new FindFailedException("list를 찾긴 찾았는데, 비어있어요");
        }
        else
        {
            throw new FindFailedException("리뷰 데이터를 찾는 도중, 알 수 없는 오류가 발생했어요");
        }
    }
    public List<NoticeEntity> findAll2(long page)
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findAll2(page,10));
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
        }

            return list;
    }


    public NoticeEntity findNoticeId(String noticeId)
    {
        NoticeEntity noticeEntity = noticeMapper.findByNoticeId(noticeId);
        return noticeEntity;

    }
    private NoticeEntity ChangeToNotice(NoticeEntity oldNoticeEntity, NoticeEntity newNoticeEntity)
    {
        NoticeEntity notice = NoticeEntity.builder()
                .noticeId(oldNoticeEntity.getNoticeId())
                .type(newNoticeEntity.getType())
                .greeting(newNoticeEntity.getGreeting())
                .titleimg(newNoticeEntity.getTitleimg())
                .title(newNoticeEntity.getTitle())
                .build();

        return notice;
    }



}
