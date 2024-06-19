package jp.co.sss.shop.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Category;
import jp.co.sss.shop.entity.Item;
/**
 * itemsテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
//    List<Item> findAllByOrderById();
    /**
     * 商品情報を登録日付順に取得 管理者機能で利用
     * @param deleteFlag 削除フラグ
     * @param pageable ページング情報
     * @return 商品エンティティのページオブジェクト
     */
    @Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag =:deleteFlag ORDER BY i.insertDate DESC,i.id DESC")
    Page<Item> findByDeleteFlagOrderByInsertDateDescPage(@Param(value = "deleteFlag") int deleteFlag, Pageable pageable);
    /**
     * 商品IDと削除フラグを条件に検索（管理者機能で利用）
     * @param id 商品ID
     * @param deleteFlag 削除フラグ
     * @return 商品エンティティ
     */
    public Item findByIdAndDeleteFlag(Integer id, int deleteFlag);
    /**
     * 商品名と削除フラグを条件に検索 (ItemValidatorで利用)
     * @param name 商品名
     * @param notDeleted 削除フラグ
     * @return 商品エンティティ
     */
    public Item findByNameAndDeleteFlag(String name, int notDeleted);
    
    Integer findStockById(Integer id);
    
    /**
     * 商品全件表示(新着順)
     */
    @Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag =:deleteFlag ORDER BY i.id DESC")
    Page<Item> findAllByOrderByIdDesc(@Param(value = "deleteFlag") int deleteFlag, Pageable pageable);
    
    
    /**
     * 新着順かつカテゴリで絞り込み
     */
    @Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag =:deleteFlag AND i.category=:category ORDER BY i.id DESC")
	Page<Item> findByCategoryOrderByIdDesc(@Param(value = "deleteFlag") int deleteFlag, @Param(value="category") Category category, Pageable pageable);
	
    /**
     * 商品全件表示(売れ筋順)
     */
    @Query("SELECT i FROM OrderItem o INNER JOIN Item i ON o. item.id=i.id WHERE i.deleteFlag =:deleteFlag GROUP BY i ORDER BY COUNT(i) DESC,i.id ASC")
    Page<Item> findAllByQuery(@Param(value = "deleteFlag") int deleteFlag, Pageable pageable);
    
    /**
     * 商品全件表示(売れ筋順) トップ画面表示用
     */
    @Query("SELECT i FROM OrderItem o INNER JOIN Item i ON o. item.id=i.id WHERE i.deleteFlag =:deleteFlag GROUP BY i ORDER BY COUNT(i) DESC,i.id ASC")
    List<Item> findAllByQuery(@Param(value = "deleteFlag") int deleteFlag);
    
    /**
     * 商品全件表示(売れ筋順かつカテゴリで絞り込み)
     */
    @Query("SELECT i FROM OrderItem o INNER JOIN Item i ON o.item.id=i.id WHERE i.deleteFlag =:deleteFlag AND i.category.id=:categoryId GROUP BY i ORDER BY COUNT(i) DESC, i.id ASC")
    Page<Item> findCategoryByQuery(@Param(value = "deleteFlag") int deleteFlag, @Param(value="categoryId")Integer categoryId, Pageable pageable);
    
	 /**
     * 商品全件表示(価格の高い順)
     */
	Page<Item> findAllByOrderByPriceDesc(Pageable pageable);
	
	 /**
     * 商品全件表示(価格の高い順)かつカテゴリで絞り込み
     */
	Page<Item> findByCategoryOrderByPriceDesc(Category category,Pageable pageable);
	
	 /**
     * 商品全件表示(価格の安い順)
     */
	Page<Item> findAllByOrderByPriceAsc(Pageable pageable);
	
	 /**
     * 商品全件表示(価格の安い順)かつカテゴリで絞り込み
     */
	Page<Item> findByCategoryOrderByPriceAsc(Category category,Pageable pageable);
	
	 /**
     * 商品名検索
     */
	Page<Item> findByNameContaining(String name, Pageable pageable);
	
}