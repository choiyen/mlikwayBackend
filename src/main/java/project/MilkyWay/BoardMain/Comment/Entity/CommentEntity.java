package project.MilkyWay.BoardMain.Comment.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import project.MilkyWay.Config.DatabaseConverter;

import javax.xml.crypto.Data;
import java.util.Objects;

@Entity
@Table(name = "Comment")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class CommentEntity
{
    @Id
    @Column(name = "commentId", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가 설정
    @Comment("PK")
    private Long commentId; // 질문을 구분하기 위한 id

    @Column(name = "boardId", nullable = false)
    @Comment("EK")
    private String boardId;  // boardDTO와 연결하기 위한 것

    @Column(name = "type", nullable = false)
    @Comment("type")
    @Convert(converter = DatabaseConverter.class)
    private String type; // 관리자인지, 사용자인지?

    @Column(name = "comment", nullable = false)
    @Comment("comment")
    @Convert(converter = DatabaseConverter.class)
    private String comment; // 댓글을 저장하기 위한 변수

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private java.util.Date createdAt;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentEntity CommentEntity = (CommentEntity) o;
        return Objects.equals(commentId, CommentEntity.commentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId);
    }
}
