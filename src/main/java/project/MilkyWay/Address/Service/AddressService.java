package project.MilkyWay.Address.Service;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.Address.DTO.AddressDTO;
import project.MilkyWay.Address.Entity.AddressEntity;

import project.MilkyWay.Administration.Service.AdministrationService;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;

import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Address.Repository.AddressRepository;
import project.MilkyWay.ComonType.LoginSuccess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class AddressService
{
    @Autowired
    AddressRepository addressRepository;


    @Autowired
    AdministrationService administrationService;

    LoginSuccess loginSuccess = new LoginSuccess();

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void scheduledDeleteOldSubmissions() {
        boolean deleted = deleteSubmissionBeforeTodayAtSixAM();
        boolean deletedadmini = administrationService.deleteByAdministrationDateBeforeTodayAtSixAM();
        if (deleted && deletedadmini) {
            System.out.println("[삭제 완료] 이전 날짜의 데이터가 성공적으로 삭제되었습니다.");
        } else {
            System.out.println("[삭제 미수행] 현재 시간이 오전 6시 이후입니다.");
        }
    }

    @PostConstruct
    public void init() {
        boolean deleted = deleteSubmissionBeforeTodayAtSixAM();
        boolean deletedadmini = administrationService.deleteByAdministrationDateBeforeTodayAtSixAM();
        if (deleted && deletedadmini) {
            System.out.println("delete check");
        } else {
            System.out.println("delete uncheck");
        }
    }

    public AddressDTO insert(AddressDTO newAddressDTO)
    {
        String uniqueId;
        do
        {
            uniqueId = loginSuccess.generateRandomId(15);
            AddressDTO addressDTO = findByAddressId(uniqueId);
            Boolean bool = administrationService.FindByAdministrationBool(uniqueId);
            if(addressDTO == null && !bool)
            {
                break;
            }
        }while (true);

        AddressEntity newAddressEntity = ConvertToEntity(newAddressDTO,uniqueId);
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
                return ConvertToDTO(addressRepository.save(newAddressEntity));
            }
        }
    }


    public AddressDTO update(AddressDTO newAddressDTO)
    {
        AddressEntity newAddressEntity = ConvertToEntity(newAddressDTO);
        AddressEntity OldAddressEntity = addressRepository.findByAddressId(newAddressEntity.getAddressId());
        if(OldAddressEntity != null)
        {
            AddressEntity ChangeAddressEntity = ConvertToEntity(OldAddressEntity, newAddressEntity);
            AddressEntity ChangeAddressEntity2 = addressRepository.save(ChangeAddressEntity);
            AddressEntity FindedAddressEntity = addressRepository.findByAddressId(ChangeAddressEntity.getAddressId());
            if(FindedAddressEntity.equals(ChangeAddressEntity2))
            {
                return ConvertToDTO(ChangeAddressEntity2);
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
    public AddressDTO findByAddressId(String EncodingAddressId)
    {
        AddressEntity addressEntity = addressRepository.findByAddressId(EncodingAddressId);
        if(addressEntity != null)
        {
            return ConvertToDTO(addressRepository.findByAddressId(EncodingAddressId));
        }
        return  null;
    }
    public  AddressDTO FindBySubmissionDate(LocalDate SubmissionDate)
    {
        return ConvertToDTO(addressRepository.findBySubmissionDate(SubmissionDate));
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


    public PageDTO findALL(int page)
    {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "submissionDate"));
        Page<AddressEntity> addressEntities = addressRepository.findAll(pageable);
        Page<AddressDTO> addressDTOs = addressEntities.map(addressEntity ->
                ConvertToDTO(addressEntity)
        );
        List<AddressDTO> addressDTOS = new ArrayList<>();
        for (AddressDTO addressDTO : addressDTOs) {
            addressDTOS.add(addressDTO);
        }
        PageDTO pageDTO = PageDTO.<AddressDTO>builder().list(addressDTOS)
                .PageCount(addressDTOs.getTotalPages())
                .Total(addressDTOs.getTotalElements()).build();

        return pageDTO;
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

    private AddressDTO ConvertToDTO(AddressEntity addressEntity1)
    {
        return AddressDTO.builder()
                .addressId(addressEntity1.getAddressId())
                .address(addressEntity1.getAddress())
                .customer(addressEntity1.getCustomer())
                .phoneNumber(addressEntity1.getPhoneNumber())
                .submissionDate(addressEntity1.getSubmissionDate())
                .acreage(addressEntity1.getAcreage())
                .cleanType(addressEntity1.getCleanType())
                .build();
    }

    private AddressEntity ConvertToEntity(AddressDTO addressDTO, String uniqueId)
    {
        return AddressEntity.builder()
                .addressId(uniqueId)
                .address(addressDTO.getAddress())
                .customer(addressDTO.getCustomer())
                .phoneNumber(addressDTO.getPhoneNumber())
                .submissionDate(addressDTO.getSubmissionDate())
                .acreage(addressDTO.getAcreage())
                .cleanType(addressDTO.getCleanType())
                .build();
    }
    private AddressEntity ConvertToEntity(AddressDTO addressDTO)
    {
        return AddressEntity.builder()
                .addressId(addressDTO.getAddressId())
                .address(addressDTO.getAddress())
                .customer(addressDTO.getCustomer())
                .phoneNumber(addressDTO.getPhoneNumber())
                .submissionDate(addressDTO.getSubmissionDate())
                .acreage(addressDTO.getAcreage())
                .cleanType(addressDTO.getCleanType())
                .build();
    }
}
//- 현재 날짜보다 고객의 의뢰 날짜가 뒷날일 떄 데이터를 파기하는 함수 필요
//고객 관리를 위한 목적의 DTO