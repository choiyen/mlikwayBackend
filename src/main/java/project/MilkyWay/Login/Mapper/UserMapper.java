package project.MilkyWay.Login.Mapper;

import org.apache.ibatis.annotations.Mapper;
import project.MilkyWay.Login.Entity.UserEntity;

import java.util.List;

@Mapper
public interface UserMapper {
    UserEntity FindByUserId(String userId);
    void deleteByUserId(String userId);
    void Insert(UserEntity user);
    void Update(UserEntity user);
    List<UserEntity> FindByEmail(String email);

}