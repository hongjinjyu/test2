package jp.co.sss.shop.controller.client.item;

import java.util.ArrayList;
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
 * 商品管理  一覧表示機能(一般会員用)のコントローラクラス
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
	public String index(Integer sortType,Model model, Pageable pageable) {
		
		//売れ筋商品を取得
		List<Item> hotList = new ArrayList<>();
		hotList =  itemRepository.findAllByQuery(Constant.NOT_DELETED);
		
		//削除フラグが１の商品のデータを取得
		List<Item> itemList = new ArrayList<>();
		itemList=itemRepository.findByDeleteFlag(Constant.NOT_DELETED);
		
		//売れ筋商品がない場合
		if (hotList.isEmpty()) {
			
			//削除フラグが１の商品がない(商品が存在しない)場合
			if(itemList.isEmpty()) {
				model.addAttribute("items",null);
			}else {	
			//新着商品を取得
			sortType = 1;
			model.addAttribute("sortTypeTop",sortType);
			model.addAttribute("items",itemRepository.findAllByOrderByIdDesc(Constant.NOT_DELETED, pageable));
			}
			
		//売れ筋商品を取得
		} else {
			sortType = 2;
			model.addAttribute("sortTypeTop",sortType);
			model.addAttribute("items", itemRepository.findAllByQuery(Constant.NOT_DELETED, pageable));
		}
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
		
		//ナビゲーションバーの商品一覧以外の検索が行われた場合
	    if (all == 0) {
	        // セッションからカテゴリIDを取得
	        Integer storedCategoryId = (Integer) session.getAttribute("categoryId");
	        
	        //カテゴリ検索で「--指定なし--」が選択された場合
	        if (categoryId != null && categoryId == 0) {
	        	// セッションからカテゴリIDを削除
	            session.removeAttribute("categoryId");
	            categoryId = null; // カテゴリIDをnullに設定
	        
	        //カテゴリーIDに値が入っている場合
	        } else
	        	if (categoryId != null) {
	            //検索されたカテゴリーIDをセッションに保存
	            session.setAttribute("categoryId", categoryId);
	        
	        	//セッションにカテゴリIDがすでに保存されている場合
	        	} else if (storedCategoryId != null) {
	            //新しく選択されたカテゴリIDを代入する
	            categoryId = storedCategoryId;
	        }
	    
	    //ナビゲーションバーで商品一覧がクリックされた場合
	    } else 
	     if(all != 0){
	        // セッションからカテゴリIDを削除
	        session.removeAttribute("categoryId");
	        categoryId = null; // カテゴリIDをnullに設定
	    }
	    

	    // カテゴリIDを使用してアイテムのリストを取得
	    Page<Item> itemsPage;
	    if (sortType == 1) {
	        // 新着順
	        if (categoryId == null || categoryId == 0) {
	        	itemsPage = itemRepository.findAllByOrderByIdDesc(Constant.NOT_DELETED, pageable);
	        //新着順かつ、カテゴリで絞り込み
	        } else {
	        	Category category = new Category();
	            category.setId(categoryId); // カテゴリIDをセット
	            itemsPage = itemRepository.findByCategoryOrderByIdDesc(Constant.NOT_DELETED, category, pageable);
	        } 
	        
	    }else if(sortType == 2){
	        // 売れ筋順
	        if (categoryId == null || categoryId == 0) {
	            itemsPage = itemRepository.findAllByQuery(Constant.NOT_DELETED, pageable);
	        //売れ筋順かつ、カテゴリで絞り込み
	        } else {
	            itemsPage = itemRepository.findCategoryByQuery(Constant.NOT_DELETED, categoryId, pageable);
	        }
	        
	    } else if(sortType == 3) {
	    	// 価格の高い順
	        if (categoryId == null || categoryId == 0) {
	            itemsPage = itemRepository.findAllByOrderByPriceDesc(Constant.NOT_DELETED,pageable);
	        //価格の高い順かつ、カテゴリで絞り込み
	        } else {
	        	Category category = new Category();
	        	category.setId(categoryId);
	            itemsPage = itemRepository.findByCategoryOrderByPriceDesc(Constant.NOT_DELETED,category, pageable);
	        }
	    
	    } else {
	    	// 価格の安い順
	    	if (categoryId == null || categoryId == 0) {
	            itemsPage = itemRepository.findAllByOrderByPriceAsc(Constant.NOT_DELETED,pageable);
	        //価格の安い順かつ、カテゴリで絞り込み
	    	} else {
	        	Category category = new Category();
	        	category.setId(categoryId);
	            itemsPage = itemRepository.findByCategoryOrderByPriceAsc(Constant.NOT_DELETED, category, pageable);
	        }
	    }
	    // ページ情報とアイテムリストをモデルに追加
	    model.addAttribute("pages", itemsPage);
	    model.addAttribute("items", itemsPage.getContent());
	    model.addAttribute("count", itemsPage.getContent().size());
	    
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
		public String showItemListName(String name, Model model, Pageable pageable) {
			
		        // セッションから検索文字列を取得
		        String newName = (String) session.getAttribute("names");
		        
		        if (name != null) {
		            //検索された文字列をセッションに保存
		            session.setAttribute("names", name);
		        
		        	//セッションに前回検索された文字列が保存されている場合
		        	} else if (newName != null) {
		            //新しく検索された文字列を代入する
		            name = newName;
		        }
			
			Page<Item> itemsPage;
			//検索された文字列を商品名に含む商品を取得
			itemsPage = itemRepository.findAllByQuery(Constant.NOT_DELETED,name, pageable);
			//セッションに保存
			model.addAttribute("pages", itemsPage);
			model.addAttribute("items", itemsPage.getContent());
			
			return "client/item/list";
		}
	
}