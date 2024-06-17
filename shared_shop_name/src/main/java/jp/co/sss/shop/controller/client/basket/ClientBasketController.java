package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.Collections;
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
	 * 買い物かごメニュー 押下
	 * @return 買い物かご一覧表示
	 */
	
	@RequestMapping(path = "/client/basket/list", method=RequestMethod.GET)
	public String basketList() {
		return "/client/basket/list";
	}
	
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
				List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
				if (basketList == null) {
					basketList = new ArrayList<>();
				}
				
				//買い物かごに商品が存在するか判定する
				boolean existItemInBasket=false;
				//BasketBean型の空のリストを生成
				BasketBean basketAddList = null;
				//買い物かごに追加された商品の情報を取得
				Item item = itemRepository.getReferenceById(id);
				//商品の在庫状況を取得
				Integer stock = item.getStock();
				
				//リストに入った順番に並べ替え
				Collections.reverse(basketList);
				
				//在庫が存在する場合
				if(stock > 0) {
					
					for (BasketBean basketAdd : basketList) {
						//basketに追加したい商品が存在する場合、注文数を増やす
						if (basketAdd.getId() == id) {
							basketAddList = basketAdd;
							int newOrderNum=basketAddList.getOrderNum()+1;
							
							//追加したい商品の個数より在庫が少ない場合
							if(newOrderNum > stock ) {
								model.addAttribute("itemNameListLessThan", item.getName());
							}else {
								basketAddList.setOrderNum(newOrderNum);
								
							}
							existItemInBasket=true;
						}
						
					}
					
					//basketに追加したい商品が存在しない場合
					if (!existItemInBasket) {
						basketAddList = new BasketBean(item.getId(), item.getName(),item.getPrice(), item.getStock(), 1);
						//買い物かごリストに追加
						basketList.add(basketAddList);
					} 
					
					
				}else
					//追加したい商品の在庫がない場合
					if(stock == 0) {
						model.addAttribute("itemNameListZero", item.getName());
					}
				
				//買い物かご内の商品の合計金額
				int totalPrice = 0;
				for(BasketBean itemInList : basketList) {
					totalPrice +=itemInList.getPrice()*itemInList.getOrderNum();
				}
				
				//買い物かご内の商品個数
				int num = 0;
				for(BasketBean itemInList : basketList) {
					num +=itemInList.getOrderNum();
				}
				
				//リストに入った順番に並べ替え
				Collections.reverse(basketList);
				
				//セッションに買い物かごを追加
				session.setAttribute("basketBeans", basketList);
				//ビューに買い物かごへ追加した商品名を登録
				model.addAttribute("cartItemName",basketAddList.getName());
				//ビューに買い物かご内の商品の合計金額を登録
				session.setAttribute("totalPrice", totalPrice);
				//ビューに買い物かご内の商品の個数を登録
				session.setAttribute("totalNum", num);
			
		return "/client/basket/list";
	}
	
	
	
	/**
	 * 削除ボタン 押下
	 * @param model
	 * @param id
	 * @return 該当商品削除後の買い物かご一覧表示
	 */
	
	@RequestMapping(path = "/client/basket/delete", method=RequestMethod.POST)
	public String basketDelete(Integer id) {
		
		@SuppressWarnings("unchecked")
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		
		BasketBean basketDelete = null;
		int i = 0;
		
		//リストに入った順番に並べ替え
		Collections.reverse(basketList);
		
		for (BasketBean list : basketList) {
			
			//該当商品の注文数が2以上の時
			if (list.getId() == id) {
				basketDelete =  list;
				//該当商品の注文数を1つ減らす
				int newOrderNum = basketDelete.getOrderNum()-1;
				basketDelete.setOrderNum(newOrderNum);
				
				//該当商品の注文数が1の時
				if(list.getOrderNum() == 0) {
					//該当商品を削除
					basketList.remove(i);
					break;
				}
			}
			
			i = i +1;
		}
		
		//basketListがnullか否か調べる
		boolean check = basketList.isEmpty();
		
		//買い物かご内の商品の合計金額
		int totalPrice = 0;
		for(BasketBean itemInList : basketList) {
			totalPrice +=itemInList.getPrice()*itemInList.getOrderNum();
		}
		
		//買い物かご内の商品個数
		int num = 0;
		for(BasketBean itemInList : basketList) {
			num +=itemInList.getOrderNum();
		}
		
		//nullの場合、買い物かご情報を全て削除
		if(check == true) {
			session.removeAttribute("basketBeans");
		
		//null出ない場合、削除後の買い物かご情報をセッションに保存
		}else {
			//リストに入った順番に並べ替え
			Collections.reverse(basketList);
			
			//セッションに買い物かごを追加
			session.setAttribute("basketBeans", basketList);
			//ビューに買い物かご内の商品の合計金額を登録
			session.setAttribute("totalPrice", totalPrice);
			//ビューに買い物かご内の商品の個数を登録
			session.setAttribute("totalNum", num);
		}
		
		return "/client/basket/list";
	}
	
	/**
	 * 削除ボタン 押下
	 * @param model
	 * @param id
	 * @return 該当商品削除後の買い物かご一覧表示
	 */
	
	@RequestMapping(path = "/client/basket/deleteItem", method=RequestMethod.POST)
	public String basketDeleteItem(Integer id) {
		
		@SuppressWarnings("unchecked")
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		
		int i = 0;
		//リストに入った順番に並べ替え
		Collections.reverse(basketList);
		
		for (BasketBean list : basketList) {
			
			//該当商品の注文数が2以上の時
			if (list.getId() == id) {
					basketList.remove(i);
					break;
				}
			
			i = i +1;
		}
		
		//basketListがnullか否か調べる
		boolean check = basketList.isEmpty();
		
		//買い物かご内の商品の合計金額
		int totalPrice = 0;
		for(BasketBean itemInList : basketList) {
			totalPrice +=itemInList.getPrice()*itemInList.getOrderNum();
		}
				
		//買い物かご内の商品個数
		int num = 0;
		for(BasketBean itemInList : basketList) {
			num +=itemInList.getOrderNum();
		}
		
		//nullの場合、買い物かご情報を全て削除
		if(check == true) {
			session.removeAttribute("basketBeans");
		
		//null出ない場合、削除後の買い物かご情報をセッションに保存
		}else {
			//リストに入った順番に並べ替え
			Collections.reverse(basketList);
			
			//セッションに買い物かごを追加
			session.setAttribute("basketBeans", basketList);
			//ビューに買い物かご内の商品の合計金額を登録
			session.setAttribute("totalPrice", totalPrice);
			//ビューに買い物かご内の商品の個数を登録
			session.setAttribute("totalNum", num);
		}
		
		return "/client/basket/list";
	}
	

	/**
	 * 買い物かごを空にするボタン 押下
	 * @param model
	 * @return 空になった買い物かごの表示
	 */
	
	@RequestMapping(path = "/client/basket/allDelete", method=RequestMethod.POST)
	public String basketDeleteAll(Model model) {
		//セッションスコープから買い物かご情報をすべて削除
		session.removeAttribute("basketBeans");
		return "/client/basket/list";
	}
	

}
