package antifraud.data;

import antifraud.entity.IP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface IPAddressRepository extends JpaRepository<IP, Long> {

    Optional<IP> findByIp(String ip);

    Iterable<IP> findAllByOrderById();

    boolean existsByIp(String ip);

    @Transactional
    void deleteByIp(String ip);
}
