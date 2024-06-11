package jp.co.sss.shop.controller.client.item;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	public String index(Model model) {
	
		return "index";
	}
	
	/**
	 * 一覧データ取得、一覧表示　処理
	 * 
	 * @param model Viewとの値受け渡し
	 * @param pageable ページ制御用
	 * @return "Client/category/list" 一覧画面 表示
	 */

	@RequestMapping(path = "/client/item/list/1", method = { RequestMethod.GET, RequestMethod.POST })
	public String showItemList(Model model, Pageable pageable) {
		
		//商品情報を全件検索(新着順)
		//表示画面でページングが必要なため、ページ情報付きの検索を行う
		Page<Item> itemsPage = itemRepository.findByDeleteFlagOrderByInsertDateDescPage(Constant.NOT_DELETED, pageable);
		
		//エンティティ内のページ情報付きの検索結果からレコードの情報だけをJavaBeansに保存
		List<Item> itemList = itemsPage.getContent();
		
		//商品情報をViewへ渡す
		model.addAttribute("pages", itemsPage);
		model.addAttribute("items", itemList);
		
		return "client/item/list";
	}
	
	/**
	 * カテゴリー検索
	 * @param categoryId
	 * @param model
	 * @param pageable
	 * @return
	 */

//	@RequestMapping(path = "/client/item/list/{sortType}?categoryId={id}", method = { RequestMethod.GET, RequestMethod.POST })
//	public  String showId(Integer categoryId, Model model) {
//		Category categories = new Category();
//	    categories.setId(categoryId);
//	    model.addAttribute("categories", categoryRepository.findByCategory(categories));
//	    return "/client/item/list";
//	}
	
	/**
	 * 商品情報詳細表示処理
	 *
	 * @param id  商品ID
	 * @param model  Viewとの値受渡し
	 * @return "client/item/detail" 詳細画面 表示
	 * 
	 * TIPS: 一般会員向けの商品詳細表示機能に類似した処理です。
	 */
	@RequestMapping(path = "/client/item/detail/{id}", method = { RequestMethod.GET, RequestMethod.POST })
	public String showItem(@PathVariable int id, Model model) {

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
