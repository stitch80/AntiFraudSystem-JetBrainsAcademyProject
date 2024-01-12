package antifraud.data;

import antifraud.entity.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    Iterable<AppUser> findAllByOrderById();

    @Transactional
    void deleteByUsername(String username);
}
