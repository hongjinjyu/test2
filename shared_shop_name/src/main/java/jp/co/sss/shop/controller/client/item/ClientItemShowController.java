package jp.co.sss.shop.controller.client.item;

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
		//Item全件をリクエストスコープに保存
		model.addAttribute("items", itemRepository.findAllByQuery(Constant.NOT_DELETED, pageable));
		return "index";
	}
	
	/**
	 * 商品一覧の表示処理
	 * 
     * @param sortType=1(新着順)、sortType=2(売れ筋順)、sortType=3(価格の高い順)、sortTyp=4(価格の安い順)
     * @param categoryId  サイドバーで選択されているカテゴリ
     * @param model  Viewとの値受渡し
     * @param pageable  ページ情報
     * @return "client/item/list"
     */
	@RequestMapping(path = "/client/item/list/{sortType}", method = { RequestMethod.GET, RequestMethod.POST })
	public String showNewItemList(@PathVariable Integer sortType,
	                               @RequestParam(name="categoryId",required = false) Integer categoryId,
	                               @RequestParam(name="all",defaultValue="0") Integer all,
	                               Model model, Pageable pageable, HttpSession session) {

	    if (all == 0) {
	        // セッションからカテゴリIDを取得
	        Integer storedCategoryId = (Integer) session.getAttribute("categoryId");

	        // セッションにカテゴリIDを保存
	        if (categoryId != null) {
	            session.setAttribute("categoryId", categoryId);
	        } else if (storedCategoryId != null) {
	            categoryId = storedCategoryId;
	        }
	    } else {
	        // セッションからカテゴリIDを削除
	        session.removeAttribute("categoryId");
	        categoryId = null; // カテゴリIDをnullに設定
	    }

	    // カテゴリIDを使用してアイテムのリストを取得
	    Page<Item> itemsPage;
	    if (sortType == 1) {
	        // 新着順
	        if (categoryId == null) {
	            itemsPage = itemRepository.findAllByOrderByIdDesc(Constant.NOT_DELETED, pageable);
	        } else {
	            Category category = new Category();  //カテゴリオブジェクト生成
	            category.setId(categoryId);
	            itemsPage = itemRepository.findByCategoryOrderByIdDesc(Constant.NOT_DELETED, category, pageable);
	        }
	    } else if(sortType == 2){
	        // 売れ筋順
	        if (categoryId == null) {
	            itemsPage = itemRepository.findAllByQuery(Constant.NOT_DELETED, pageable);
	        } else {
	            itemsPage = itemRepository.findCategoryByQuery(Constant.NOT_DELETED, categoryId, pageable);
	        }
	    } else if(sortType == 3) {
	    	// 価格の高い順
	        if (categoryId == null) {
	            itemsPage = itemRepository.findAllByOrderByPriceDesc(pageable);
	        } else {
	        	Category category = new Category();
	        	category.setId(categoryId);
	            itemsPage = itemRepository.findByCategoryOrderByPriceDesc(category, pageable);
	        }
	    } else {
	    	// 価格の安い順
	    	if (categoryId == null) {
	            itemsPage = itemRepository.findAllByOrderByPriceAsc(pageable);
	        } else {
	        	Category category = new Category();
	        	category.setId(categoryId);
	            itemsPage = itemRepository.findByCategoryOrderByPriceAsc(category, pageable);
	        }
	    }
	    // ページ情報とアイテムリストをモデルに追加
	    model.addAttribute("pages", itemsPage);
	    model.addAttribute("items", itemsPage.getContent());

	    return "client/item/list";
	}
	
	/**
	 * 商品詳細表示
	 * @param id  Viewからクリックされた商品のID
	 * @param model  Viewとの値受渡し
	 * @return
	 */
	@RequestMapping(path = "/client/item/detail/{id}", method = RequestMethod.GET)
	public String showItem(@PathVariable int id, Model model) {
		//選択されたItemの情報をリクエストスコープに保存
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
        //詳細画面に遷移
		return "client/item/detail";
	}
	
	//商品名検索
	@RequestMapping(path = "/searchName", method = RequestMethod.GET)
	public String showItemListName(String name, Model model/*, Pageable pageable*/) {
		model.addAttribute("items", itemRepository.findByNameContaining(name/*, pageable*/));
		return "client/item/list";
	}
	
}