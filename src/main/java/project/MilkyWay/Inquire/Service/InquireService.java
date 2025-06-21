package project.MilkyWay.Inquire.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Inquire.DTO.InquireDTO;
import project.MilkyWay.Inquire.Entity.InquireEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Inquire.Repository.InqurieRepository;

import java.util.ArrayList;
import java.util.List;


@Service
public class InquireService
{
    @Autowired
    InqurieRepository inqurieRepository;

    public InquireDTO Insert(InquireDTO inquireDTOs)
    {

        String uniqueId;
        LoginSuccess loginSuccess = new LoginSuccess();
        do
        {
            uniqueId = loginSuccess.generateRandomId(15);
            if(!inqurieRepository.existsByInquireId(uniqueId))
            {
                break;
            }
        }while (true);

            InquireEntity inquireEntity = ConvertToEntity(inquireDTOs, uniqueId);
            InquireEntity inquireEntity1 = inqurieRepository.save(inquireEntity);
            if (inquireEntity1 != null) {
                return ConvertToDTO(inquireEntity1);
            } else {
                throw new InsertFailedException("날짜 가능 문의 등록이 실패하였습니다.");
            }

    }
    public InquireDTO Check(String encodinginquireId)
    {

        InquireEntity inquireEntity1 = inqurieRepository.findByInquireId(encodinginquireId);
        if(inquireEntity1 != null)
        {
            InquireEntity newinquireEntity2 = ConvertToNewCheck(inquireEntity1);
            InquireEntity inquireEntity2 = inqurieRepository.save(newinquireEntity2);
            InquireEntity saveinquire = inqurieRepository.findByInquireId(newinquireEntity2.getInquireId());
            if(saveinquire.equals(inquireEntity2))
            {
                return ConvertToDTO(inquireEntity2);
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
    public InquireDTO FindByInquireId(String encodingInquireId)
    {
        return ConvertToDTO(inqurieRepository.findByInquireId(encodingInquireId));
    }
    private boolean existByinquireId(String inquireId)
    {
        return inqurieRepository.existsByInquireId(inquireId);
    }
    public PageDTO findAll(int page)
    {
        Pageable pageable = PageRequest.of(page, 10);
        Page<InquireEntity> InquireEntity = inqurieRepository.findAll(pageable);
        if(InquireEntity != null)
        {
            List<InquireDTO> inquireDTOS = new ArrayList<>();
            for(InquireEntity inquireEntity : InquireEntity)
            {
                inquireDTOS.add(ConvertToDTO(inquireEntity));
            }
            PageDTO pageDTO = PageDTO.<InquireDTO>builder().list(inquireDTOS)
                    .PageCount(InquireEntity.getTotalPages())
                    .Total(InquireEntity.getTotalElements()).build();

            return pageDTO;
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
    private InquireEntity ConvertToEntity(InquireDTO inquireDTO)
    {
        return InquireEntity.builder()
                .inquireId(inquireDTO.getInquireId())
                .address(inquireDTO.getAddress())
                .phoneNumber(inquireDTO.getPhoneNumber())
                .inquire(inquireDTO.getInquire())
                .dateOfInquiry(inquireDTO.getDateOfInquiry())
                .inquirename(inquireDTO.getInquirename())
                .build();
    }
    //uniqueId
    private InquireEntity ConvertToEntity(InquireDTO inquireDTO, String uniqueId)
    {
        return InquireEntity.builder()
                .inquireId(uniqueId)
                .address(inquireDTO.getAddress())
                .phoneNumber(inquireDTO.getPhoneNumber())
                .inquire(inquireDTO.getInquire())
                .inquirename(inquireDTO.getInquirename())
                .dateOfInquiry(inquireDTO.getDateOfInquiry())
                .inquireBool(inquireDTO.getInquireBool())
                .build();
    }

    private InquireDTO ConvertToDTO(InquireEntity inquireEntity)
    {
        return InquireDTO.builder()
                .inquireId(inquireEntity.getInquireId())
                .address(inquireEntity.getAddress())
                .phoneNumber(inquireEntity.getPhoneNumber())
                .inquire(inquireEntity.getInquire())
                .dateOfInquiry(inquireEntity.getDateOfInquiry())
                .inquirename(inquireEntity.getInquirename())
                .inquireBool(inquireEntity.getInquireBool())
                .build();
    }
}
//- 상담 신청이 들어온 날짜에서 1주일이 지날 경우, 자동 페기하는 스케줄러 등록
//상담 신청을 받기 위한 DTO