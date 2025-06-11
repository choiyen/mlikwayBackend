package project.MilkyWay.Login.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import project.MilkyWay.Config.DatabaseConverter;

@Entity
@Table(name = "User")
@AllArgsConstructor(access = AccessLevel.PUBLIC)  // 생성자 접근 수준을 PUBLIC으로 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UserEntity
{
    @Id
    @Column(name = "userId")
    @Comment("PK")
    private String userId; //아이디

    @Column(name = "password")
    @Comment("password")
    private String password; //비밀번호

    @Column(name = "email")
    @Comment("이메일")
    private String email; //비밀번호 찾기 구현을 위한 이메일
}
