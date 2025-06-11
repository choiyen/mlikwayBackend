package project.MilkyWay.BoardMain.Board.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.BoardMain.Comment.Entity.CommentEntity;
import project.MilkyWay.Config.DatabaseConverter;

import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "board")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@ToString
public class BoardEntity
{
    @Id
    @Column(name = "boardId", nullable = false)
    @Comment("PK")
    private String boardId;

    @Column(name = "title", nullable = false)
    @Comment("title")
    @Convert(converter = DatabaseConverter.class)
    private String title; // 게시판 질문의 제목을 저장하는 변수

    @Column(name = "content", nullable = false)
    @Comment("content")
    @Convert(converter = DatabaseConverter.class)
    private String content; // 게시판의 내용을 저장하는 변수

    @Column(name = "password")
    @Comment("게시판비번")
    @Convert(converter = DatabaseConverter.class)
    private String password; //비밀번호

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "boardId", referencedColumnName = "boardId", insertable = false, updatable = false)
    public Collection<CommentEntity> commentEntities; // Collection으로 변경

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardEntity BoardEntity = (BoardEntity) o;
        return Objects.equals(boardId, BoardEntity.boardId) &&
                Objects.equals(title, BoardEntity.title) &&
                Objects.equals(content, BoardEntity.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId, title, content);
    }

}
