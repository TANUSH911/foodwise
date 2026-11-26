package com.foodwise.repository;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SQL access for {@code food_item}. Spring Data writes the SQL for the
 * derived method names; @Query methods are JPQL that Hibernate turns into SQL.
 */
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByStatusOrderByExpiryDateAsc(ItemStatus status);

    /** Items expiring on or before {@code date} (includes already expired ones). */
    List<FoodItem> findByStatusAndExpiryDateLessThanEqual(ItemStatus status, LocalDate date);

    long countByStatus(ItemStatus status);

    long countByStatusAndStatusDateGreaterThanEqual(ItemStatus status, LocalDate from);

    @Query("select sum(f.price) from FoodItem f where f.status = :status and f.statusDate >= :from")
    BigDecimal sumPriceByStatusSince(@Param("status") ItemStatus status, @Param("from") LocalDate from);

    /** Count and cost per month, e.g. SELECT YEAR(status_date), MONTH(status_date), COUNT(*), SUM(price) ... GROUP BY 1, 2 */
    @Query("""
            select new com.foodwise.repository.MonthlyWaste(
                year(f.statusDate), month(f.statusDate), count(f), sum(f.price))
            from FoodItem f
            where f.status = :status and f.statusDate >= :from
            group by year(f.statusDate), month(f.statusDate)
            order by year(f.statusDate), month(f.statusDate)
            """)
    List<MonthlyWaste> monthlyTotals(@Param("status") ItemStatus status, @Param("from") LocalDate from);

    @Query("""
            select new com.foodwise.repository.CategoryWaste(f.category, count(f), sum(f.price))
            from FoodItem f
            where f.status = :status and f.statusDate >= :from
            group by f.category
            order by count(f) desc
            """)
    List<CategoryWaste> categoryTotals(@Param("status") ItemStatus status, @Param("from") LocalDate from);
}
