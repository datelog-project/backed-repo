package me.jinheum.datelog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
public interface UserConnectionRepository extends JpaRepository<UserConnection , UUID> {
    

    @Query("SELECT COUNT(uc) > 0 FROM UserConnection uc WHERE (uc.user IN :users OR uc.partner IN :users) AND uc.status = 'PENDING'")
    boolean existsPendingConnectionForUsers(@Param("users") List<UserAccount> users); //이미 초대 보냈는지 한번에 가져와서 쿼리 줄임



    @Query("SELECT uc FROM UserConnection uc WHERE " +
        "(uc.user = :user1 AND uc.partner = :user2) OR " +
        "(uc.user = :user2 AND uc.partner = :user1)")
    Optional<UserConnection> findByUserAndPartner(@Param("user1") UserAccount user1, @Param("user2") UserAccount user2);
    //원래 초대한 사람만 재연결 가능, 초대 받은 사람만 END 가능 했는데 양방향으로 바꿔봄
}
