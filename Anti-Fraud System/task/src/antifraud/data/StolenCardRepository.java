package antifraud.data;

import antifraud.entity.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

    Optional<StolenCard> findByNumber(String number);

    Iterable<StolenCard> findAllByOrderByNumber();

    boolean existsByNumber(String number);

    @Transactional
    void deleteByNumber(String number);
}
