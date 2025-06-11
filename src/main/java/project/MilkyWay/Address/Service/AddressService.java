package project.MilkyWay.Address.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.Address.Entity.AddressEntity;

import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;

import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Address.Repository.AddressRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class AddressService
{
    @Autowired
    AddressRepository addressRepository;

    public AddressEntity insert(AddressEntity newAddressEntity)
    {
        if(addressRepository.existsByAddressId(newAddressEntity.getAddressId()))
        {
            throw new InsertFailedException("같은 주소Id를 가진 데이터가 존재합니다. 변경 후 시도해주세요.");
        }
        else
        {
            if(addressRepository.existsBySubmissionDate(newAddressEntity.getSubmissionDate()))
            {
                throw new InsertFailedException("같은 날짜를 가진 일정이 존재합니다. 변경 후 시도해주세요.");
            }
            else
            {
                return addressRepository.save(newAddressEntity);
            }
        }
    }
    public AddressEntity update(AddressEntity newAddressEntity)
    {
        AddressEntity OldAddressEntity = addressRepository.findByAddressId(newAddressEntity.getAddressId());
        if(OldAddressEntity != null)
        {
            AddressEntity ChangeAddressEntity = ConvertToEntity(OldAddressEntity, newAddressEntity);
            AddressEntity ChangeAddressEntity2 = addressRepository.save(ChangeAddressEntity);
            AddressEntity FindedAddressEntity = addressRepository.findByAddressId(ChangeAddressEntity.getAddressId());
            if(FindedAddressEntity.equals(ChangeAddressEntity2))
            {
                return ChangeAddressEntity2;
            }
            else
            {
                throw new UpdateFailedException("고객 주소 데이터가 잘못되어 변경을 시도했으나, 수정에 실패했습니다.");
            }
        }
        else
        {
            throw new FindFailedException("옛날에 저장한 고객의 주소 데이터를 찾지 못했어요. 업데이트가 불가능합니다.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean Delete(String EncodingAddressId)
    {
        boolean bool = addressRepository.existsByAddressId(EncodingAddressId);
        if(bool)
        {
            addressRepository.deleteByAddressId(EncodingAddressId);
            boolean bool2 = addressRepository.existsByAddressId(EncodingAddressId);
            if(bool2)
            {
                throw new DeleteFailedException("고객 주소 데이터 삭제에 실패했습니다. 관리자에게 문의하세요");
            }
            else
            {
                return true;
            }
        }
        else
        {
            throw new DeleteFailedException("삭제할 고객 주소 데이터를 찾을 수 없습니다. 관리자에게 문의하세요");
        }
    }
    public AddressEntity findByAddressId(String EncodingAddressId)
    {
        return addressRepository.findByAddressId(EncodingAddressId);
    }
    public  AddressEntity FindBySubmissionDate(LocalDate SubmissionDate)
    {
        return addressRepository.findBySubmissionDate(SubmissionDate);
    }
    @Transactional
    public boolean deleteSubmissionBeforeTodayAtSixAM() {
        LocalTime now = LocalTime.now();
        LocalTime sixPM = LocalTime.of(18, 0);

        // 현재 시간이 오후 6시 이전라면 삭제하지 않음
        if (now.isBefore(sixPM)) {
            return false;
        }

        // 오늘보다 이전 날짜의 데이터를 삭제
        LocalDate endOfYesterday = LocalDate.now(); // 오늘 00시
        addressRepository.deleteBySubmissionDateBefore(endOfYesterday);

        return true;
    }


    public Page<AddressEntity> findALL(int page)
    {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "submissionDate"));
        Page<AddressEntity> addressEntities = addressRepository.findAll(pageable);
        return addressEntities;
    }
    private AddressEntity ConvertToEntity(AddressEntity oldAddressEntity, AddressEntity newAddressEntity)
    {
        return AddressEntity.builder()
                .addressId(oldAddressEntity.getAddressId())
                .address(newAddressEntity.getAddress())
                .phoneNumber(newAddressEntity.getPhoneNumber())
                .customer(newAddressEntity.getCustomer())
                .submissionDate(newAddressEntity.getSubmissionDate())
                .acreage(newAddressEntity.getAcreage())
                .cleanType(newAddressEntity.getCleanType())
                .build();
    }
}
//- 현재 날짜보다 고객의 의뢰 날짜가 뒷날일 떄 데이터를 파기하는 함수 필요
//고객 관리를 위한 목적의 DTO