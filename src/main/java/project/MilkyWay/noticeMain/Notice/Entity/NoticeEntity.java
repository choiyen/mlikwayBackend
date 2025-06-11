package project.MilkyWay.noticeMain.Notice.Entity;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.Config.DatabaseConverter;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;

import java.util.Collection;

@Entity
@Table(name = "Notice")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class NoticeEntity
{
    @Id
    @Column(name = "NoticeId")
    @Comment("PK")
    private String noticeId; // 후기 ID : primary key이자 10자리의 렌덤키

    @Column(name = "title", nullable = false)
    @Comment("title에는 주소가 포함될 확률이 있음")
    private String title;


    @Column(name = "titleimg", nullable = false)
    @Comment("타이틀이미지")
    private String titleimg; // 청소 후 사진

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("청소유형")
    private CleanType type; // 어떤 유형의 일 : 이사청소, 입주청소, 주거청소.....

    @Lob
    @Column(name = "greeting", nullable = false,columnDefinition = "TEXT")
    @Comment("서문")
    private String greeting;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "NoticeId", referencedColumnName = "NoticeId")
    public Collection<NoticeDetailEntity> noticeDetailEntities;
}
