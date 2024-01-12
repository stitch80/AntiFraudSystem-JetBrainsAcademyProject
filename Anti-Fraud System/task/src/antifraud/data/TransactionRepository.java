package antifraud.data;

import antifraud.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT count(distinct(ip)) FROM TRANSACTION where number = :number and date between :start and :end ;", nativeQuery = true)
    int countIpAddressesInLastHour(
            @Param("number") String number,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT distinct(ip) FROM TRANSACTION where number = :number and date between :start and :end ;", nativeQuery = true)
    Iterable<String> getIpAddressesInLastHour(
            @Param("number") String number,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT count(distinct(region)) FROM TRANSACTION where number = :number and date between :start and :end ;", nativeQuery = true)
    int countRegionsInLastHour(
            @Param("number") String number,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT distinct(region) FROM TRANSACTION where number = :number and date between :start and :end ;", nativeQuery = true)
    Iterable<String> getRegionsInLastHour(
            @Param("number") String number,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Iterable<Transaction> findAllByNumber(String number);
}
