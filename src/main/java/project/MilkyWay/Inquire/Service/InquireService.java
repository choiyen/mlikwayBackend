package project.MilkyWay.Inquire.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.Inquire.Entity.InquireEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Inquire.Repository.InqurieRepository;


@Service
public class InquireService
{
    @Autowired
    InqurieRepository inqurieRepository;

    public InquireEntity Insert(InquireEntity inquireEntity)
    {
        boolean bool = inqurieRepository.existsByInquireId(inquireEntity.getInquireId());
        if(bool)
        {
            throw new FindFailedException("해당 inquireId를 가진 정보는 이미 존재해요.");
        }
        else
        {
            InquireEntity inquireEntity1 = inqurieRepository.save(inquireEntity);
            if (inquireEntity1 != null) {
                return inquireEntity1;
            } else {
                throw new InsertFailedException("날짜 가능 문의 등록이 실패하였습니다.");
            }
        }
    }
    public InquireEntity Update(String encodinginquireId)
    {

        InquireEntity inquireEntity1 = inqurieRepository.findByInquireId(encodinginquireId);
        if(inquireEntity1 != null)
        {
            InquireEntity newinquireEntity2 = ConvertToNewCheck(inquireEntity1);
            InquireEntity inquireEntity2 = inqurieRepository.save(newinquireEntity2);
            InquireEntity saveinquire = inqurieRepository.findByInquireId(newinquireEntity2.getInquireId());
            if(saveinquire.equals(inquireEntity2))
            {
                return inquireEntity2;
            }
            else
            {
                throw new UpdateFailedException("inquire 클래스의 변경을 시도했으나, 변경되지 않았습니다.");
            }
        }
        else
        {
            throw new FindFailedException("수정할 질문 내역을 찾지 못하였습니다.");
        }
    }
    public InquireEntity FindByInquireId(String encodingInquireId)
    {
        InquireEntity inquireEntity = inqurieRepository.findByInquireId(encodingInquireId);
            return inquireEntity;
    }
    private boolean existByinquireId(String inquireId)
    {
        return inqurieRepository.existsByInquireId(inquireId);
    }
    public Page<InquireEntity> findAll(int page)
    {
        Pageable pageable = PageRequest.of(page, 10);
        Page<InquireEntity> InquireEntity = inqurieRepository.findAll(pageable);
        if(InquireEntity != null)
        {
            return InquireEntity;
        }
        else
        {
            throw new FindFailedException("알 수 없는 오류로 데이터 조회에 실패하였습니다.");
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean Delete(String encodingInquireId)
    {
            if(inqurieRepository.existsByInquireId(encodingInquireId)) {
                inqurieRepository.deleteByInquireId(encodingInquireId);
                boolean bool = existByinquireId(encodingInquireId);
                if (bool) {
                    throw new DeleteFailedException("데이터 삭제에 실패한 것 같아요. 다시 시도해주세요.");
                } else {
                    return true;
                }
            }
            else
            {
                throw new FindFailedException("이미 삭제가 되었거나, 삭제할 문의 Id를 찾을 수 없습니다.");
            }
    }
    private InquireEntity ConvertToNewCheck(InquireEntity oldinquireEntity)
    {
        return InquireEntity.builder()
                .inquireId(oldinquireEntity.getInquireId())
                .inquire(oldinquireEntity.getInquire())
                .phoneNumber(oldinquireEntity.getPhoneNumber())
                .address(oldinquireEntity.getAddress())
                .dateOfInquiry(oldinquireEntity.getDateOfInquiry())
                .inquirename(oldinquireEntity.getInquirename())
                .inquireBool(!oldinquireEntity.getInquireBool())
                .build();
    }

}
//- 상담 신청이 들어온 날짜에서 1주일이 지날 경우, 자동 페기하는 스케줄러 등록
//상담 신청을 받기 위한 DTO