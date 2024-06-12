package jp.co.sss.shop.controller.client.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.entity.Category;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.CategoryRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;

/**
 * 商品管理 一覧表示機能(一般会員用)のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class ClientItemShowController {
	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;
	
	@Autowired
	CategoryRepository categoryRepository;
	
	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;
	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 */
	@Autowired
	BeanTools beanTools;
	
	/**
	 * トップ画面 表示処理
	 *
	 * @param model    Viewとの値受渡し
	 * @return "index" トップ画面
	 */
	@RequestMapping(path = "/" , method = { RequestMethod.GET, RequestMethod.POST })
	public String index(Model model, Pageable pageable) {
		model.addAttribute("items", itemRepository.findAllByQuery(pageable));
	
		return "index";
	}
	
    /**
     * @param sortType=1(新着順)、sortType=2(売れ筋順)
     * @param categoryId
     * @param model
     * @param pageable
     * @return "client/item/list"
     */

	@RequestMapping(path = "/client/item/list/{sortType}", method = { RequestMethod.GET, RequestMethod.POST })
	public String showItemList(@PathVariable Integer sortType, @RequestParam(name="categoryId", defaultValue="0",required = false) Integer categoryId,Model model, Pageable pageable) {
		
		Category category = new Category();
		category.setId(categoryId);
		if(sortType == 1) { //新着順
			if(categoryId == 0) {
				Page<Item> itemsNewPage = itemRepository.findAllByOrderByInsertDateDesc(pageable);
				List<Item> itemNewList = itemsNewPage.getContent();
				model.addAttribute("pages", itemsNewPage);
				model.addAttribute("items", itemNewList);
		    } else {
		    	Page<Item> itemsCategoryPage = itemRepository.findByCategoryOrderByInsertDateDesc(category, pageable);
				//エンティティ内のページ情報付きの検索結果からレコードの情報だけをJavaBeansに保存
				List<Item> itemCategoryList =itemsCategoryPage.getContent();
				model.addAttribute("pages", itemsCategoryPage);
				model.addAttribute("items", itemCategoryList);
		    }
		
		} else { //売れ筋順
			if(categoryId == 0) {
				Page<Item> favoriteItemsPage = itemRepository.findAllByQuery(pageable);
				//エンティティ内のページ情報付きの検索結果からレコードの情報だけをJavaBeansに保存
				List<Item> favoriteItemList = favoriteItemsPage.getContent();
				model.addAttribute("pages", favoriteItemsPage);
				model.addAttribute("items",  favoriteItemList);
				
			} else {
				Page<Item> favoriteItemsCategoryPage = itemRepository.findCategoryByQuery(categoryId,pageable);
				//エンティティ内のページ情報付きの検索結果からレコードの情報だけをJavaBeansに保存
				List<Item> favoriteitemCategoryList = favoriteItemsCategoryPage.getContent();
				model.addAttribute("pages", favoriteItemsCategoryPage);
				model.addAttribute("items",favoriteitemCategoryList);
			}
		}
		return "client/item/list";
	}
	
	/**
	 * 商品詳細表示
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "/client/item/detail/{id}", method = RequestMethod.GET)
	public String showItem(@PathVariable int id, Model model) {
		model.addAttribute("items", itemRepository.getReferenceById(id));
		// 対象の商品情報を取得
				Item item = itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED);

				if (item == null) {
					// 対象が無い場合、エラー
					return "redirect:/syserror";
				}

				//Itemエンティティの各フィールドの値をItemBeanにコピー
				ItemBean itemBean = beanTools.copyEntityToItemBean(item);

				// 商品情報をViewへ渡す
				model.addAttribute("item", itemBean);

		return "client/item/detail";
	}
	
}
