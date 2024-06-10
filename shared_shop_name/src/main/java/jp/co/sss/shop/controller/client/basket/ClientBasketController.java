package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;

@Controller
public class ClientBasketController {
	
	/**
	 * セッション情報
	 */
	@Autowired
	HttpSession session;
	
	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;
	
	
	/**
	 * 買い物かごへ追加ボタン 押下
	 * 
	 * @param model
	 * @param id　追加する商品ID
	 * @return 買い物かご一覧表示
	 */
	
	//買い物かごへ追加ボタン　押下
	@RequestMapping(path = "/client/basket/add", method=RequestMethod.POST)
	public String basketAdd(Model model, Integer id) {
		
		//basketがセッションにない場合、basketList生成
				@SuppressWarnings("unchecked")
				List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBean");
				if (basketList == null) {
					basketList = new ArrayList<>();
				}
				
				boolean existItemInBasket=false;
				BasketBean basketAddList = null;
				Item item = itemRepository.getReferenceById(id);
				
				//basketに追加したい商品が存在する場合、注文数を増やす
				for (BasketBean basketAdd : basketList) {
					if (basketAdd.getId() == id) {
						basketAddList = basketAdd;
						int newOrderNum=basketAddList.getOrderNum()+1;
						basketAddList.setOrderNum(newOrderNum);
						existItemInBasket=true;
					} 
				}
				
				
				//basketに追加したい商品が存在しない場合
				if (!existItemInBasket) {
					basketAddList=new BasketBean();
					basketAddList.setId(item.getId());
					basketAddList.setName(item.getName());
					basketAddList.setStock(item.getStock());
					int orderNum = 1;
					basketAddList.setOrderNum(orderNum);
					//買い物かごリストに追加
					basketList.add(basketAddList);
				} 
				
				//追加したい商品の在庫がない場合
				if(item.getStock() == null) {
					model.addAttribute("itemNameListZero", item.getName());
				}
				
				//追加したい商品の個数より在庫が少ない場合
				if(basketAddList.getOrderNum() > item.getStock()) {
					model.addAttribute("itemNameListLessThan", item.getName());
				}
				
				//セッションに買い物かごを追加
				session.setAttribute("basketBean", basketList);
				//ビューに買い物かごへ追加した商品名を登録
				model.addAttribute("cartItemName",basketAddList.getName());
			
		return "/client/basket/list";
	}
	
	
	/**
	 * 削除ボタン 押下
	 * @param model
	 * @param id
	 * @return 該当商品削除後の買い物かご一覧表示
	 */
	
	/**
	@RequestMapping(path = " /client/basket/delete/{id}", method=RequestMethod.POST)
	public String basketDelete(Model model, Integer id) {
		
		BasketBean basketDelete = (BasketBean) session.getAttribute("basketBean");
		int orderNum = basketDelete.getOrderNum();
		
		//該当商品の注文数が1の時
		if(orderNum == 1) {
			String deleteId = id.toString();
			session.removeAttribute(deleteId);
		
		}else
			//該当商品の注文数が2以上の場合
			if(orderNum >= 2) {
				
				basketAddList.setOrderNum(newOrderNum);
			}
		return "/client/basket/list";
	}
	**/
	
	
	/**
	 * 買い物かごを空にするボタン 押下
	 * @param model
	 * @return 空になった買い物かごの表示
	 */
	
	@RequestMapping(path = "/basket/deleteAll", method=RequestMethod.POST)
	public String basketDeleteAll(Model model) {
		session.removeAttribute("basketBean");
		return "/client/basket/list";
	}
	
	
	/**
	 * 買い物かごメニュー 押下
	 * @return 買い物かご一覧表示
	 */
	
	@RequestMapping(path = "/client/basket/list", method=RequestMethod.GET)
	public String basketList() {
		
		@SuppressWarnings("unchecked")
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBean");
		if (basketList == null) {
			basketList = new ArrayList<>();
			session.setAttribute("basketBean", basketList);
		}
		
		return "/client/basket/list";
	}

}
