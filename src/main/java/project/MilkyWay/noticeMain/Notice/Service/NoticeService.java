package project.MilkyWay.noticeMain.Notice.Service;


import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.S3ClientService.S3ImageService;
import project.MilkyWay.noticeMain.Notice.DTO.NoticeDTO;
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

   LoginSuccess loginSuccess = new LoginSuccess();

   @Autowired
   private S3ImageService s3ImageService;

    public NoticeDTO InsertNotice(NoticeDTO noticeDTO, MultipartFile titleimg)
   {
       String uniqueId;
       do
       {
           uniqueId = loginSuccess.generateRandomId(15);
           NoticeDTO notice1 = findNoticeId(uniqueId);
           if(notice1 == null)
           {
               break;
           }
       }while (true);


       String url = uploading(titleimg);
       NoticeEntity noticeEntity = ConvertToNotice(noticeDTO, uniqueId, url);

       noticeMapper.Insert(noticeEntity);
       NoticeEntity notice = noticeMapper.findByNoticeId(noticeEntity.getNoticeId());
       if(notice != null)
       {
           return ConvertToNotice(notice);
       }
       else
       {
           throw new InsertFailedException("데이터베이스에 데이터를 추가 시키러고 시도했는데, 실패했나봐요ㅠㅠㅠ");
       }
   }


   public NoticeDTO UpdateNotice(NoticeDTO newNoticeDTO, MultipartFile titleimg)
   {
       NoticeDTO oldnotice = findNoticeId(newNoticeDTO.getNoticeId());
       String Titleurl;
       if (titleimg != null && !titleimg.isEmpty())
       {
           Titleurl = uploading(titleimg);
           FileDelete(oldnotice.getTitleimg());
       }
       else
       {
           Titleurl = newNoticeDTO.getTitleimg();
       }
       NoticeEntity noticeEntity = ConvertToNotice(newNoticeDTO,Titleurl);
       if(oldnotice != null)
       {
           NoticeEntity newNoticeEntity2 = ChangeToNotice(ConvertToNotice(oldnotice), noticeEntity);
           noticeMapper.Update(newNoticeEntity2);
           NoticeEntity SelectnewNoticeEntity = noticeMapper.findByNoticeId(newNoticeEntity2.getNoticeId());
           if(SelectnewNoticeEntity.getNoticeId().equals(newNoticeEntity2.getNoticeId()) && SelectnewNoticeEntity.getType().equals(newNoticeEntity2.getType())&&SelectnewNoticeEntity.getGreeting().equals(newNoticeEntity2.getGreeting()))
           {
               return ConvertToNotice(SelectnewNoticeEntity);
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

    private NoticeEntity ConvertToNotice(NoticeDTO noticeDTO)
    {return NoticeEntity.builder()
            .noticeId(noticeDTO.getNoticeId())
            .type(noticeDTO.getType())
            .titleimg(noticeDTO.getTitleimg())
            .greeting(noticeDTO.getGreeting())
            .build();
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
    public List<NoticeDTO> findSmallAll(CleanType type, Long page)
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findByType(type.name(), page, 10));
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
        }

        List<NoticeDTO> noticeDTOS = new ArrayList<>();
        if(noticeDTOS != null)
        {
            for(NoticeEntity noticeEntity : list)
            {
                noticeDTOS.add(ConvertToNotice(noticeEntity));
            }
            return noticeDTOS;
        }
        else
        {
            throw new FindFailedException("예기치 못한 오류가 발생하였습니다.");
        }
    }

    public List<NoticeDTO> findAll()
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findAll());
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
        }

        if(list.isEmpty() != true)
        {
            List <NoticeDTO> noticeDTOS = new ArrayList<>();
            for(NoticeEntity notice : list)
            {
                noticeDTOS.add(ConvertToNotice(notice));
            }
            return noticeDTOS;
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
    public List<NoticeDTO> findAll2(long page)
    {
        List<NoticeEntity> list = new ArrayList<>(noticeMapper.findAll2(page,10));
        List<NoticeDTO> list1 = new ArrayList<>();
        for(NoticeEntity notice : list)
        {
            Hibernate.initialize(notice.getNoticeDetailEntities());
            list1.add(ConvertToNotice(notice));
        }

            return list1;
    }


    public NoticeDTO findNoticeId(String noticeId)
    {
        NoticeEntity noticeEntity = noticeMapper.findByNoticeId(noticeId);
        if(noticeEntity != null) {
            return ConvertToNotice(noticeEntity);
        }
        return  null;

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
    private NoticeEntity ConvertToNotice(NoticeDTO noticeDTO, String Titleurl)
    {
        return NoticeEntity.builder()
                .noticeId(noticeDTO.getNoticeId())
                .type(noticeDTO.getType())
                .greeting(noticeDTO.getGreeting())
                .titleimg(Titleurl)
                .title(noticeDTO.getTitle())
                .build();
    }
    private NoticeEntity ConvertToNotice(NoticeDTO noticeDTO, String uniqueId, String url)
    {
        return NoticeEntity.builder()
                .noticeId(uniqueId)
                .type(noticeDTO.getType())
                .greeting(noticeDTO.getGreeting())
                .titleimg(url)
                .title(noticeDTO.getTitle())
                .build();
    }
    private NoticeDTO ConvertToNotice(NoticeEntity noticeEntity)
    {
        return NoticeDTO.builder()
                .noticeId(noticeEntity.getNoticeId())
                .type(noticeEntity.getType())
                .greeting(noticeEntity.getGreeting())
                .titleimg(noticeEntity.getTitleimg())
                .title(noticeEntity.getTitle())
                .build();
    }
    public String uploading(MultipartFile titleimg)
    {

        try {
            return s3ImageService.upload(titleimg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void FileDelete(String url)
    {
        try {
            s3ImageService.deleteImageFromS3(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public  void FileDelete(List<String> urllist)
    {
        try {
            for(String url : urllist)
            {
                s3ImageService.deleteImageFromS3(url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
