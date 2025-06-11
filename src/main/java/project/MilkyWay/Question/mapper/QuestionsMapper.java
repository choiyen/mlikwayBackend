package project.MilkyWay.Question.mapper;

import org.apache.ibatis.annotations.Mapper;
import project.MilkyWay.Question.Entity.QuestionsEntity;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;


import java.util.List;


@Mapper
public interface QuestionsMapper
{
    List<QuestionsEntity> findAll();
    QuestionsEntity findById(Long Id);
    void deleteById(Long Id);
    void Insert(QuestionsEntity questionsEntity);
    void Update(QuestionsEntity questionsEntity);
    List<QuestionsEntity> findAll2(Integer offset, Integer limit);
    Long totalRecord();
}
