package antifraud.data;

import antifraud.entity.CardLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardLimitRepository extends JpaRepository<CardLimit, Long> {

    Optional<CardLimit> findByCardNumber (String cardNumber);

//    boolean existsByLimitName(String limitName);
//
//    @Query(value = "select limit_value from transaction_limit where limit_name = :limitName", nativeQuery = true)
//    int findLimitValueByLimitName(@Param("limitName") String limitName);
//
//    @Transactional
//    @Modifying
//    @Query(value = "update transaction_limit set limit_value = :limitValue where limit_name = :limitName", nativeQuery = true)
//    void updateLimitValue(@Param("limitValue") int limitValue, @Param("limitName") String limitName);
}
