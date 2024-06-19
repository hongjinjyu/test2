package jp.co.sss.shop.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

	boolean existsByUserAndItem(User user, Item item);
	
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findByUserId(@Param("userId") int userId);

    
    @Query("SELECT f FROM Favorite f WHERE f.item.id = :itemId")
    List<Favorite> findByItemId(@Param("itemId") int itemId);
	
    @Query("SELECT COUNT(f) FROM Favorite f")
    int countFavorites();
    
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    Page<Favorite> findByUserId(@Param("userId") int userId,Pageable pageable);
    
    @Query("SELECT f.id FROM Favorite f WHERE f.item.id = :itemId")
    int findFavoriteIdByItemId(@Param("itemId") int itemId);
    
    @Query("SELECT f.id FROM Favorite f WHERE f.item.id = :itemId AND f.user.id = :userId")
    Integer findFavoriteIdByItemIdAndUserId(@Param("itemId") int itemId, @Param("userId") int userId);

	
}