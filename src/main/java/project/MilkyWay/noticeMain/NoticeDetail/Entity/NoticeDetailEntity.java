package project.MilkyWay.noticeMain.NoticeDetail.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.ComonType.Enum.Cleandirection;
import project.MilkyWay.ComonType.StringListTypeHandler;

import java.util.List;

/**
 * - NoticeDTO와 NoticedetaillDTO는 1대 다 관계로 묶인다.
 * - NotciedetaillDTO가 저장되지 않으면 NoticeDTO를 삭제하는 로직 필요
 */
@Entity
@Table(name = "NoticeDetail")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class NoticeDetailEntity
{

    @Id
    @Column(name = "NoticeDetailId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가 설정
    @Comment("PK")
    private Long noticeDetailId; // 1씩 증가하는 auto inclement로 작성


    @Column(name = "NoticeId")
    @Comment("EK")
    private String noticeId; // NoticeDTO와 연결하기 위한 왜래키

    @Enumerated(EnumType.STRING)  // `enum` 값을 문자열로 저장
    @Column(name = "direction")
    @Comment("방종류")
    private Cleandirection direction; // 방 위치 중에 어디? - 기실, 방, 화장실, 베란다

    @Column(name = "beforeURL", columnDefinition = "TEXT")
    @Comment("청소전 사진")
    private List<String> beforeURL; // 청소 전 사진


    @Column(name = "afterURL", columnDefinition = "TEXT")
    @Comment("청소후 사진")
    private List<String> afterURL; // 청소 후 사진


    @Lob
    @Column(name = "comment", nullable = false,columnDefinition = "TEXT")
    private String comment; // 해당 구역을 청소하고 느낀점 기록

}
