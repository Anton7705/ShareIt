package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime before, LocalDateTime after);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime before);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime after);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime before, LocalDateTime after);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime before);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime after);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);


    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findPastBookingsForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findFutureBookingsForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND b.end < :now ORDER BY b.item.id, b.end DESC")
    List<Booking> findPastBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND b.start > :now ORDER BY b.item.id, b.start ASC")
    List<Booking> findFutureBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
            "b.booker.id = :userId AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' AND b.end < :now")
    boolean existsCompletedBooking(@Param("userId") Long userId,
                                   @Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);
}
