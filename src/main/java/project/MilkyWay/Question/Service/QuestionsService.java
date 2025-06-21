package project.MilkyWay.Question.Service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.MilkyWay.Question.DTO.QuestionsDTO;
import project.MilkyWay.Question.Entity.QuestionsEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Question.mapper.QuestionsMapper;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionsService //고객 질문을 관리하기 위한 DTO
{
    @Autowired
    QuestionsMapper questionsMapper;

public List<QuestionsDTO> Insertquestion(QuestionsDTO questionsDTO)
    {
        QuestionsEntity newQuestionEntity = ConVertToEntity(questionsDTO);
        questionsMapper.Insert(newQuestionEntity);
        List<QuestionsEntity> questionsEntity = questionsMapper.findAll();
        if(questionsEntity != null)
        {
            List<QuestionsDTO> questionsDTOS = new ArrayList<>();
            for(QuestionsEntity questionsEntity1 : questionsEntity)
            {
                questionsDTOS.add(ConVertToDTO(questionsEntity1));
            }
            return  questionsDTOS;
        }
        else
        {
            throw new InsertFailedException("해당 질문 데이터를 추가할 수 없습니다.");
        }
    }
    public QuestionsDTO updatequestion(QuestionsDTO newquestionsDTO)
    {
        QuestionsEntity newQuestionEntity = ConVertToEntity(newquestionsDTO);
        QuestionsEntity OldQuestion = questionsMapper.findById(newquestionsDTO.getId());
        if(OldQuestion != null)
        {
            QuestionsEntity changeQuestionEntity = ChangeToQuestion(OldQuestion, newQuestionEntity);
            questionsMapper.Update(changeQuestionEntity);
            QuestionsEntity ChangeDate = questionsMapper.findById(OldQuestion.getId());
            if(ChangeDate.getId().equals(changeQuestionEntity.getId()) && ChangeDate.getExceptionQA().equals(changeQuestionEntity.getExceptionQA())&&ChangeDate.getExpectedComment().equals(changeQuestionEntity.getExpectedComment()))
            {
                return ConVertToDTO(ChangeDate);
            }
            else
            {
                throw new UpdateFailedException("변경에 실패했는지, 변경 데이터와 요청 데이터가 달라요");
            }
        }
        else
        {
            throw new FindFailedException("해당 질문코드를 지닌 질문을 찾지 못하기에 수정할 수 없습니다");
        }

    }
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean DeleteByQuestionId(Long encodingQuestionId) {
        QuestionsEntity questionsEntity = questionsMapper.findById(encodingQuestionId);
        if(questionsEntity != null)
        {
            questionsMapper.deleteById(encodingQuestionId);
            QuestionsEntity questionsEntity2 = questionsMapper.findById(encodingQuestionId);
            if(questionsEntity2 == null)
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
            throw new FindFailedException("데이터를 지울려고 했는데, 질문 Id에 맞는 정보가 없네요");
        }
    }

    public List<QuestionsDTO> findAll()
    {
        List<QuestionsEntity> list = questionsMapper.findAll();
        List<QuestionsDTO> questionsDTOS = new ArrayList<>();
        for(QuestionsEntity questionsEntity : list)
        {
            questionsDTOS.add(ConVertToDTO(questionsEntity));
        }
        return questionsDTOS;
    }
    public QuestionsDTO SelectQuestion(Long encodingquestionId)
    {
        QuestionsEntity questionsEntity = questionsMapper.findById(encodingquestionId);
        if(questionsEntity != null)
        {
            return ConVertToDTO(questionsEntity);
        }
        else
        {
            throw new FindFailedException("해당 질문코드를 지닌 데이터는 없어요.");
        }
    }
    public List<QuestionsDTO> findAll2(Integer page)
    {
        List<QuestionsEntity> list = new ArrayList<>(questionsMapper.findAll2(page, 10));
        List<QuestionsDTO> questionsDTOS = new ArrayList<>();

        for(QuestionsEntity questionsEntity : list)
        {
            questionsDTOS.add(ConVertToDTO(questionsEntity));
        }
        return questionsDTOS;
    }
    public Long totalRecord() {
        return questionsMapper.totalRecord();
    }
    public Integer totalPaging() {
        Long totalRecords = totalRecord(); // 전체 레코드 수
        int pageSize = 100;               // 한 페이지에 보여줄 수

        return (int) Math.ceil((double) totalRecords / pageSize);
    }


    private QuestionsEntity ChangeToQuestion(QuestionsEntity questionsEntity, QuestionsEntity newQuestionEntity)
    {

        return QuestionsEntity.builder()
                .id(questionsEntity.getId())
                .exceptionQA(newQuestionEntity.getExceptionQA())
                .expectedComment(newQuestionEntity.getExpectedComment())
                .build();
    }

    private QuestionsEntity ConVertToEntity(QuestionsDTO questionsDTO)
    {
        return QuestionsEntity.builder()
                .id(questionsDTO.getId())
                .exceptionQA(questionsDTO.getExceptionQA())
                .expectedComment(questionsDTO.getExpectedComment())
                .build();
    }
    private QuestionsDTO ConVertToDTO(QuestionsEntity questionsEntity)
    {
        return QuestionsDTO.builder()
                .id(questionsEntity.getId())
                .exceptionQA(questionsEntity.getExceptionQA())
                .expectedComment(questionsEntity.getExpectedComment())
                .build();
    }
}
