package project.MilkyWay.noticeMain.NoticeDetail.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.MilkyWay.S3ClientService.S3ImageService;
import project.MilkyWay.noticeMain.NoticeDetail.DTO.NoticeDetailDTO;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.noticeMain.NoticeDetail.mapper.NoticeDetailMapper;


import java.util.List;



@Service
public class NoticeDetailService {
    @Autowired
    NoticeDetailMapper noticeDetailMapper;


    @Autowired
    S3ImageService s3ImageService;

    public NoticeDetailDTO InsertNoticeDetallMapper(NoticeDetailDTO noticeDetailDTO, String noticeId, List<String> beforeUrls, List<String> afterUrls) {

        NoticeDetailEntity newNoticeDetailEntity = ConvertToNoticeDetail(noticeDetailDTO,noticeId, beforeUrls, afterUrls);
        noticeDetailMapper.Insert(newNoticeDetailEntity);
        NoticeDetailEntity noticeDetailEntity = noticeDetailMapper.findByNoticeDetailId(newNoticeDetailEntity.getNoticeDetailId());
        if (noticeDetailEntity != null) {
            return ConvertToNoticeDetail(noticeDetailEntity);
        } else {
            throw new InsertFailedException("데이터를 추가 시키려고 시도했는데, 실패했나봐요");
        }
    }

    private NoticeDetailDTO ConvertToNoticeDetail(NoticeDetailEntity noticeDetailEntity)
    {
        return NoticeDetailDTO.builder()
                .noticeDetailId(noticeDetailEntity.getNoticeDetailId())
                .noticeId(noticeDetailEntity.getNoticeId())
                .beforeURL(noticeDetailEntity.getBeforeURL())
                .afterURL(noticeDetailEntity.getAfterURL())
                .direction(noticeDetailEntity.getDirection())
                .comment(noticeDetailEntity.getComment())
                .build();
    }

    public NoticeDetailDTO UpdateNoticeDetailMapper(NoticeDetailDTO ChangingNoticeDetailDTO, List<String> beforeUrls,  List<String> afterUrls)
    {
        NoticeDetailEntity noticeDetailEntity = ConvertToNoticeDetail(ChangingNoticeDetailDTO, beforeUrls,afterUrls);
        NoticeDetailEntity OldNoticeDetailEntity = noticeDetailMapper.findByNoticeDetailId(ChangingNoticeDetailDTO.getNoticeDetailId());
        if (OldNoticeDetailEntity != null)
        {
            NoticeDetailEntity newNoticeDetailEntity2 = ChangeToNoticeDetail(OldNoticeDetailEntity, noticeDetailEntity);
            noticeDetailMapper.Update(newNoticeDetailEntity2);
            NoticeDetailEntity SelectnewNoticeEntity = noticeDetailMapper.findByNoticeDetailId(ChangingNoticeDetailDTO.getNoticeDetailId());
            if (!SelectnewNoticeEntity.equals(newNoticeDetailEntity2))
            {
                return ConvertToNoticeDetail(SelectnewNoticeEntity);
            }
            else
            {
                throw new FindFailedException(ChangingNoticeDetailDTO.getNoticeDetailId() + "데이터 수정을 시도할 수 있었는데, 세부 내역 수정엔 실패했네요. 관리자에게 문의하세요");
            }
        }
        return ConvertToNoticeDetail(noticeDetailEntity);
    }

    public NoticeDetailDTO noticeDetail(Long NoticeDetailId)
    {
        return ConvertToNoticeDetail(noticeDetailMapper.findByNoticeDetailId(NoticeDetailId));
    }
    public List<NoticeDetailEntity> ListNoticeDetail(String encodingNoticeId)
    {
        List<NoticeDetailEntity> list = noticeDetailMapper.findByNoticeId(encodingNoticeId);
        if(list == null)
        {
            throw new FindFailedException("데이터 찾기를 시도했는데, 알 수 없는 오류가 발생했어요. 관리자에게 문의해줘요");
        }

        else
        {
            return list;
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean DeleteToNoticeDetail(Long encodingNoticeDetail)
    {
        NoticeDetailEntity seach = noticeDetailMapper.findByNoticeDetailId(encodingNoticeDetail);
        if(seach != null)
        {
            noticeDetailMapper.deleteByNoticeDetailId(encodingNoticeDetail);
            NoticeDetailEntity noticeDetailEntity = noticeDetailMapper.findByNoticeDetailId(encodingNoticeDetail);
            if(noticeDetailEntity == null)
            {
                Deleteimage(seach.getBeforeURL());
                Deleteimage(seach.getAfterURL());
                return true;
            }
            else
            {
                throw new DeleteFailedException("데이터베이스에서 삭제하는데 실패한 것 같아요. 관리자에게 문의해줘요");
            }
        }
        else
        {
          throw  new RuntimeException("데이터베이스에서 삭제할 데이터를 못찾겠어요. 관리자에게 문의해주세요.");
        }
    }
    public void Deleteimage(List <String> URL)
    {
        for (String Deleteimage : URL)
        {
            s3ImageService.deleteImageFromS3(Deleteimage);
        }
    }
    private NoticeDetailEntity ChangeToNoticeDetail(NoticeDetailEntity oldNoticeDetailEntity, NoticeDetailEntity changingNoticeDetailEntity)
    {
        NoticeDetailEntity noticeDetailEntity = NoticeDetailEntity.builder()
                .noticeDetailId(oldNoticeDetailEntity.getNoticeDetailId())
                .noticeId(oldNoticeDetailEntity.getNoticeId())
                .afterURL(changingNoticeDetailEntity.getAfterURL())
                .beforeURL(changingNoticeDetailEntity.getBeforeURL())
                .direction(changingNoticeDetailEntity.getDirection())
                .comment(changingNoticeDetailEntity.getComment())
                .build();

        return noticeDetailEntity;

    }

    private NoticeDetailEntity ConvertToNoticeDetail(NoticeDetailDTO noticeDetailDTO, String noticeId, List<String> beforeUrls, List<String> AfterUrls)
    {
        return NoticeDetailEntity.builder()
                .noticeId(noticeId)
                .noticeDetailId(noticeDetailDTO.getNoticeDetailId())
                .direction(noticeDetailDTO.getDirection())
                .beforeURL(beforeUrls)
                .afterURL(AfterUrls)
                .comment(noticeDetailDTO.getComment())
                .build();
    }

    private NoticeDetailEntity ConvertToNoticeDetail(NoticeDetailDTO noticeDetailDTO, List<String> beforeUrls, List<String> AfterUrls)
    {
        return NoticeDetailEntity.builder()
                .noticeId(noticeDetailDTO.getNoticeId())
                .noticeDetailId(noticeDetailDTO.getNoticeDetailId())
                .direction(noticeDetailDTO.getDirection())
                .beforeURL(beforeUrls)
                .afterURL(AfterUrls)
                .comment(noticeDetailDTO.getComment())
                .build();
    }


}

/**
 * - NoticeDTO와 NoticedetaillDTO는 1대 다 관계로 묶인다.
 * - NotciedetaillDTO가 저장되지 않으면 NoticeDTO를 삭제하는 로직 필요
 */