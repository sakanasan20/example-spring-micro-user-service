package tw.niq.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.niq.example.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmail(String email);
	
	Optional<UserEntity> findByUserId(String userId);
	
}
