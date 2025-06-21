package project.MilkyWay.Administration.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.Administration.DTO.AdministrationDTO;
import project.MilkyWay.Administration.Entity.AdministrationEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Administration.Repository.AdministrationRepository;
import project.MilkyWay.ComonType.LoginSuccess;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdministrationService
{
    @Autowired
    AdministrationRepository administrationRepository;

    LoginSuccess loginSuccess = new LoginSuccess();

    public AdministrationDTO insert(AdministrationDTO administrationDTO)
    {
        String uniqueId;
        do
        {
            uniqueId = loginSuccess.generateRandomId(15);
            boolean bool = FindByAdministrationBool(uniqueId);
            if(!bool)
            {
                break;
            }
        }while (true);
        AdministrationEntity administration = ConvertToEntity(administrationDTO, uniqueId);
        AdministrationEntity administrationEntity = administrationRepository.save(administration);
        if(administrationEntity != null)
        {
            return ConvertToDTO(administrationEntity);
        }
        else
        {
            throw new InsertFailedException("데이터 저장을 시도하였으나, 정상적으로 입력되지 않았습니다.");
        }
    }
    public AdministrationDTO insert(AdministrationDTO administrationDTO, String uniqueId)
    {
        AdministrationEntity administration = ConvertToEntity(administrationDTO, uniqueId);
        AdministrationEntity administrationEntity = administrationRepository.save(administration);
        if(administrationEntity != null)
        {
            return ConvertToDTO(administrationEntity);
        }
        else
        {
            throw new InsertFailedException("데이터 저장을 시도하였으나, 정상적으로 입력되지 않았습니다.");
        }
    }
    public AdministrationDTO Update(AdministrationDTO administrationDTO)
    {
        AdministrationEntity administration = ConvertToEntity(administrationDTO);
        AdministrationEntity oldAdmin = administrationRepository.findByAdministrationId(administration.getAdministrationId());
        if(oldAdmin != null)
        {
            AdministrationEntity newAdmin = ConVertToEntity(oldAdmin, administration);
            AdministrationEntity changeAdmin = administrationRepository.save(newAdmin);
            AdministrationEntity findAdmin = administrationRepository.findByAdministrationId(changeAdmin.getAdministrationId());
            if(findAdmin.equals(changeAdmin))
            {
                return ConvertToDTO(changeAdmin);
            }
            else
            {
                throw new UpdateFailedException("데이터 변경을 시도했으나, 변경되지 않았습니다.");
            }
        }
        else
        {
            throw new FindFailedException("기존 일정 데이터를 찾을 수 없습니다.");
        }
    }
    public boolean exists(String EncodingAdminsistrationId)
    {
        return administrationRepository.existsByAdministrationId(EncodingAdminsistrationId);
    }
    public boolean existsByDate(LocalDate AdminsistrationDate)
    {
        return administrationRepository.existsByAdministrationDate(AdminsistrationDate);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean Delete(String EncodingAdministrationId)
    {
        boolean bool = administrationRepository.existsByAdministrationId(EncodingAdministrationId);
        if (bool)
        {
            administrationRepository.deleteByAdministrationId(EncodingAdministrationId);
            boolean bool2 = administrationRepository.existsByAdministrationId(EncodingAdministrationId);
            if(bool2)
            {
                throw new DeleteFailedException("일정 삭제를 시도했는데, 삭제가 되지 않고 남아있어요");
            }
            else
            {
                return bool;
            }
        }
        else
        {
            throw new FindFailedException("해당 코드로 삭제할 수 있는 일정표가 존재하지 않아요");
        }
    }
    @Transactional
    public boolean deleteByAdministrationDateBeforeTodayAtSixAM() {
        LocalTime now = LocalTime.now();
        LocalTime sixPM = LocalTime.of(18, 0);

        // 현재 시간이 오후 6시 이전라면 삭제하지 않음
        if (now.isBefore(sixPM)) {
            return false;
        }

        // 오늘보다 이전 날짜의 데이터를 삭제
        LocalDate endOfYesterday = LocalDate.now(); // 오늘 00시
        administrationRepository.deleteByAdministrationDateBefore(endOfYesterday);
        return true;
    }

    public List<AdministrationDTO> FindAll()
    {
        List<AdministrationEntity> administrationEntities = administrationRepository.findAll();
        if(administrationEntities != null)
        {
            List<AdministrationDTO> administrationDTOS = new ArrayList<>();
            for(AdministrationEntity administration : administrationEntities)
            {
                administrationDTOS.add(ConvertToDTO(administration));
            }
            return administrationDTOS;
        }
        else
        {
            throw new FindFailedException("알 수 없는 오류로 데이터베이스 정보를 찾을 수 없어요! 다시 시도해주세요");
        }
    }
    public List<AdministrationDTO> FindByAdministrationDateBetween(LocalDate Date)
    {
        LocalDate LastDate = Date.plusDays(30);
        List<AdministrationEntity> administrationEntities = administrationRepository.findByAdministrationDateBetween(Date,LastDate);
        if(administrationEntities.isEmpty())
        {
            throw new FindFailedException("일정 데이터를 찾았는데, 데이터베이스가 비어 있어요");
        }
        else if(administrationEntities != null)
        {
            List<AdministrationDTO> administrationDTOS = new ArrayList<>();
            for(AdministrationEntity administration : administrationEntities)
            {
                administrationDTOS.add(ConvertToDTO(administration));
            }
            return administrationDTOS;
        }
        else
        {
            throw new FindFailedException("알 수 없는 오류로 데이터베이스 정보를 찾을 수 없어요! 다시 시도해주세요");
        }
    }

    public AdministrationDTO FindByAdministration(String EncodingAdministrationId)
    {
        AdministrationEntity AdministrationEntity = administrationRepository.findByAdministrationId(EncodingAdministrationId);
        if(AdministrationEntity != null)
        {
            return  ConvertToDTO(AdministrationEntity);
        }
        else
        {
            throw new FindFailedException("일정 데이터를 찾을 수 없었어요.");
        }
    }
    public Boolean FindByAdministrationBool(String EncodingAdministrationId)
    {
        AdministrationEntity AdministrationEntity = administrationRepository.findByAdministrationId(EncodingAdministrationId);
        if(AdministrationEntity != null)
        {
            return  true;
        }
        else
        {
            return false;
        }
    }
    private AdministrationEntity ConVertToEntity(AdministrationEntity oldAdmin, AdministrationEntity administration)
    {
        return AdministrationEntity.builder()
                .administrationId(oldAdmin.getAdministrationId())
                .adminstrationType(administration.getAdminstrationType())
                .administrationDate(administration.getAdministrationDate())
                .build();
    }


    public AdministrationEntity FindByAdministrationDate(LocalDate subissionDate)
    {
        AdministrationEntity AdministrationEntity = administrationRepository.findByAdministrationDate(subissionDate);
        return  AdministrationEntity;
    }

    private AdministrationDTO ConvertToDTO(AdministrationEntity administrationEntity)
    {
        return AdministrationDTO.builder()
                .administrationId(administrationEntity.getAdministrationId())
                .administrationDate(administrationEntity.getAdministrationDate())
                .adminstrationType(administrationEntity.getAdminstrationType())
                .build();
    }

    private AdministrationEntity ConvertToEntity(AdministrationDTO administrationDTO)
    {
        return AdministrationEntity.builder()
                .administrationId(administrationDTO.getAdministrationId())
                .adminstrationType(administrationDTO.getAdminstrationType())
                .administrationDate(administrationDTO.getAdministrationDate())
                .build();
    }
    private AdministrationEntity ConvertToEntity(AdministrationDTO administrationDTO, String uniqueId)
    {
        return AdministrationEntity.builder()
                .administrationId(uniqueId)
                .adminstrationType(administrationDTO.getAdminstrationType())
                .administrationDate(administrationDTO.getAdministrationDate())
                .build();
    }
}
