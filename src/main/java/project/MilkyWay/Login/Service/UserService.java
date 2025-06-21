package project.MilkyWay.Login.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.MilkyWay.Login.DTO.LoginDTO;
import project.MilkyWay.Login.DTO.UserDTO;
import project.MilkyWay.Login.Entity.UserEntity;
import project.MilkyWay.ComonType.Expection.DeleteFailedException;
import project.MilkyWay.ComonType.Expection.FindFailedException;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.ComonType.Expection.UpdateFailedException;
import project.MilkyWay.Login.Mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService //관리자 아이디를 관리하는 DTO
{
    @Autowired
    private UserMapper userMapper;

  public UserDTO createUser(UserDTO userDTO, PasswordEncoder passwordEncoder)
  {
      UserEntity userEntity = ConvertToEntity(userDTO, passwordEncoder);
      UserEntity user2 = userMapper.FindByUserId(userEntity.getUserId());
      if(user2 == null)
      {
          userMapper.Insert(userEntity);
          UserEntity newUser = userMapper.FindByUserId(userEntity.getUserId());
          if(newUser != null)
          {
              return ConvertToDTO(newUser);
          }
          else
          {
              throw new InsertFailedException("관리자 아이디 생성에 실패하였습니다.");
          }
      }
      else
      {
          throw new FindFailedException("이미 존재하는 관리자 아이디라 실패하였습니다.");
      }

  }
  public UserDTO existUser(LoginDTO loginDTO)
  {
      UserEntity newUser = userMapper.FindByUserId(loginDTO.getUserId());
      if(newUser != null)
      {
          return ConvertToDTO(newUser);
      }
      else
      {
          throw new FindFailedException("아이디나 비밀번호가 틀렸거나, 가입된 관리자 계정이 없습니다.");
      }
  }




  public UserDTO UpdateUser(UserDTO NewuserDTO, PasswordEncoder passwordEncoder)
  {
      UserEntity userEntity = ConvertToEntity(NewuserDTO, passwordEncoder);
      UserEntity previousUser = userMapper.FindByUserId(userEntity.getUserId());
      if(previousUser != null)
      {
          UserEntity ChangeUser = ChangeUserEntity(previousUser, userEntity);
          userMapper.Update(ChangeUser);
          UserEntity newUser = userMapper.FindByUserId(ChangeUser.getUserId());
          if(newUser.getUserId().equals(ChangeUser.getUserId()) && newUser.getPassword().equals(ChangeUser.getPassword())&& newUser.getEmail().equals(ChangeUser.getEmail()))
          {
              return ConvertToDTO(newUser);
          }
          else
          {
              throw new UpdateFailedException();
          }
      }
      else
      {
          throw new FindFailedException("회원정보에 해당 아이디는 존재하지 않아요");
      }
  }
  public boolean DeleteUser(String userId)
  {
      UserEntity user = userMapper.FindByUserId(userId);
      if(user != null)
      {
          userMapper.deleteByUserId(userId);
          return true;
      }
      else
      {
          throw new DeleteFailedException("삭제할 아이디가 없거나 정보가 틀립니다.");
      }
  }
  public List<UserDTO> findEmail(String email)
  {
        List<UserEntity> user = userMapper.FindByEmail(email);
        if(user != null)
        {
            List<UserDTO> userDTOS = new ArrayList<>();
            for(UserEntity userEntity : user)
            {
                userDTOS.add(ConvertToDTO(userEntity));
            }
            return userDTOS;
        }
        else
        {
            throw new FindFailedException("해당 정보의 회원은 존재하지 않아요.");
        }
  }
  private UserEntity ChangeUserEntity(UserEntity previousUser, UserEntity newUser)
  {
      return UserEntity.builder()
              .userId(previousUser.getUserId())
              .password(newUser.getPassword())
              .email(newUser.getEmail())
              .build();
  }

    private UserEntity ConvertToEntity(UserDTO userDTO, PasswordEncoder passwordEncoder)
    {
        return UserEntity.builder()
                .userId(userDTO.getUserId())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .build();
    }
    private UserDTO ConvertToDTO(UserEntity userEntity)
    {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .password(userEntity.getPassword())
                .build();
    }
}
