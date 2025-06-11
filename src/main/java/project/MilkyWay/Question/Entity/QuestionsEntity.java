package project.MilkyWay.Question.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;


@Entity
@Table(name = "Question")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class QuestionsEntity
{
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT 설정을 사용할 때는 Identity 전략 사용
    @Comment("PK")
    private Long id; // Q&A 질문을 등록하기 위한 것

    @Column(name = "exceptionQA", nullable = false)
    private String exceptionQA; // 예상했던 Q&A 질문
    @Column(name = "expectedComment", nullable = false)
    private String expectedComment; // 예상질문에 대한 해답


}
