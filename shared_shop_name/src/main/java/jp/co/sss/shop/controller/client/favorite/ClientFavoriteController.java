package jp.co.sss.shop.controller.client.favorite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.service.FavoriteService;
/**
 * 会員管理 削除機能(一般会員)のコントローラクラス
 * @author ジ・ジョンヒョン
 */
@Controller
public class ClientFavoriteController {

	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private FavoriteService favoriteService;

	@RequestMapping(path = "/addFavorite", method = RequestMethod.POST)
	public String addFavorite(Model model, Integer id, RedirectAttributes re, HttpSession session) {

		//現在ログイン中のユーザーの情報を取得、id保存
		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = new User();
		BeanUtils.copyProperties(userBean, user);
		int userId = user.getId();

		//当itemを収納
		Optional<Item> selectedItem = itemRepository.findById(id);
		Item item = selectedItem.get();//optionalのメソッド

		//Favoriteテーブルのレコードを全権検索、行数保存
		List<Favorite> favoriteList = new ArrayList<>();
		favoriteList = favoriteRepository.findAll();
		int countFavorites = favoriteRepository.countFavorites();

		//追加するか判定するためのBoolean変数
		boolean addFavorite = true;

		//データベースにセッションのuserIdと　当商品のitemIdと　レコード確認
		//あったらFalseなかったらtrue
		for (int i = 0; i < countFavorites; i++) {
			int iItemId = favoriteList.get(i).getItem().getId();
			int iUserId = favoriteList.get(i).getUser().getId();
			if (iItemId == id && iUserId == userId) {
				addFavorite = false;
				break;
			} else {
				addFavorite = true;
			}
		}
		//追加する作業
		if (user != null && item != null && addFavorite == true) {
			Favorite favorite = new Favorite();
			favorite.setUser(user);
			favorite.setItem(item);
			favoriteRepository.save(favorite);
			return "redirect:/showFavoriteList"; // 즐겨찾기 추가 후 목록 페이지로 리다이렉트
		} else {

			re.addFlashAttribute("errorMessageFavoriteAdded", 1);

			// 유효하지 않은 사용자 또는 아이템 처리
			return "redirect:/client/item/detail/" + id; // 예시로 목록 페이지로 리다이렉트, 에러 처리 필요시 별도로 구현
		}
	}

	@RequestMapping(path = "/showFavoriteList", method = RequestMethod.GET)
	public String showFavoriteList(HttpSession session, Model model, Pageable pageable) {
		//現在ログイン中のユーザーの情報を取得、id保存
		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = new User();
		BeanUtils.copyProperties(userBean, user);
		int userId = user.getId();
		//ページング
		Page<Favorite> favoritesPage;
		favoritesPage = favoriteRepository.findByUserId(userId, pageable);
		int favoriteListNoneFlag = 0;
		if(favoritesPage.getTotalElements()==0) {
			favoriteListNoneFlag=1;
		}
		//リクエストスコープ
		model.addAttribute("favoriteListNoneFlag",favoriteListNoneFlag);
		model.addAttribute("pages", favoritesPage);
		model.addAttribute("items", favoritesPage.getContent());

		return "client/item/FavoriteList";
	}

	@RequestMapping(path = "/deleteFavorite", method = RequestMethod.GET)
	public String deleteFavorite(HttpSession session, Model model, Pageable pageable, Integer id) {
		//現在ログイン中のユーザーの情報を取得、id保存
		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = new User();
		BeanUtils.copyProperties(userBean, user);
		int userId = user.getId(); //げんざいろぐいんしてるにｎ

		//favoriteListそこのIdを参照して持ってくる作業
		List<Favorite> favoriteList = new ArrayList<>();
		favoriteList = favoriteRepository.findByUserId(userId);

		//その中から今押してる商品（Integer id）が同じ場合、そのレコードを消す作業
		int kesu = favoriteRepository.findFavoriteIdByItemIdAndUserId(id, userId);

		favoriteRepository.deleteById(kesu);

		Page<Favorite> favoritesPage;
		favoritesPage = favoriteRepository.findByUserId(userId, pageable);

		model.addAttribute("pages", favoritesPage);
		model.addAttribute("items", favoritesPage.getContent());

		return "redirect:/showFavoriteList";
	}

	/**
	 * 買い物かごへ追加ボタン 押下
	 * 
	 * @param model 
	 * @param id　追加する商品ID
	 * @return 買い物かご一覧表示
	 */

	//買い物かごへ追加ボタン　押下
	@RequestMapping(path = "client/basket/addAndDel", method = RequestMethod.POST)
	public String basketAdd(Model model, Integer id, RedirectAttributes re, HttpSession session, Pageable pageable) {

		//basketがセッションにない場合、basketList生成
		@SuppressWarnings("unchecked")
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBeans");
		if (basketList == null) {
			basketList = new ArrayList<>();
		}

		//買い物かごに商品が存在するか判定する
		boolean existItemInBasket = false;
		//BasketBean型の空のリストを生成
		BasketBean basketAddList = null;
		//買い物かごに追加された商品の情報を取得
		Item item = itemRepository.getReferenceById(id);
		//商品の在庫状況を取得
		Integer stock = item.getStock();

		//リストに入った順番に並べ替え
		Collections.reverse(basketList);

		//在庫が存在する場合
		if (stock > 0) {

			for (BasketBean basketAdd : basketList) {
				//basketに追加したい商品が存在する場合、注文数を増やす
				if (basketAdd.getId() == id) {
					basketAddList = basketAdd;
					int newOrderNum = basketAddList.getOrderNum() + 1;

					//追加したい商品の個数より在庫が少ない場合
					if (newOrderNum > stock) {
						re.addFlashAttribute("itemNameListLessThan", item.getName());
					} else {
						basketAddList.setOrderNum(newOrderNum);

					}
					existItemInBasket = true;
				}

			}

			//basketに追加したい商品が存在しない場合
			if (!existItemInBasket) {
				basketAddList = new BasketBean(item.getId(), item.getName(), item.getPrice(), item.getStock(), 1);
				//買い物かごリストに追加
				basketList.add(basketAddList);
			}

		} else
		//追加したい商品の在庫がない場合
		if (stock == 0) {
			model.addAttribute("itemNameListZero", item.getName());
		}

		//買い物かご内の商品の合計金額
		int totalPrice = 0;
		for (BasketBean itemInList : basketList) {
			totalPrice += itemInList.getPrice() * itemInList.getOrderNum();
		}

		//買い物かご内の商品個数
		int num = 0;
		for (BasketBean itemInList : basketList) {
			num += itemInList.getOrderNum();
		}

		//リストに入った順番に並べ替え
		Collections.reverse(basketList);

		//セッションに買い物かごを追加
		session.setAttribute("basketBeans", basketList);
		//ビューに買い物かごへ追加した商品名を登録
		model.addAttribute("cartItemName", basketAddList.getName());
		//ビューに買い物かご内の商品の合計金額を登録
		session.setAttribute("totalPrice", totalPrice);
		//ビューに買い物かご内の商品の個数を登録
		session.setAttribute("totalNum", num);

		UserBean userBean = (UserBean) session.getAttribute("user");
		User user = new User();
		BeanUtils.copyProperties(userBean, user);
		int userId = user.getId(); //げんざいろぐいんしてるにｎ

		//favoriteListそこのIdを参照して持ってくる作業
		List<Favorite> favoriteList = new ArrayList<>();
		favoriteList = favoriteRepository.findByUserId(userId);
		//↑は今ログインしている人の気に入りリストだけ入っています。
		//ので、その中から今押してる商品（Integer id）が同じ場合、そのレコードを消す作業をします。

		int kesu = favoriteRepository.findFavoriteIdByItemIdAndUserId(id, userId);

		favoriteRepository.deleteById(kesu);

		Page<Favorite> favoritesPage;
		favoritesPage = favoriteRepository.findByUserId(userId, pageable);

		model.addAttribute("pages", favoritesPage);
		model.addAttribute("items", favoritesPage.getContent());

		return "redirect:/client/basket/list";

	}

}
