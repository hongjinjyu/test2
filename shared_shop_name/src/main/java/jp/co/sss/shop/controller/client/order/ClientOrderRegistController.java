package jp.co.sss.shop.controller.client.order;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
/*
 * 注文機能のコントローラクラス
 * 
 * @author 畑田鉄平
 * 
 */

@Controller
public class ClientOrderRegistController {

	@Autowired
	HttpSession session;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * ご注文のお手続きボタン 押下時処理
	 * @return 届け先入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.POST)
	public String inputOrder() {
		OrderForm orderForm = new OrderForm();
		//セッションスコープからログイン会員情報を取得
		UserBean userBean = (UserBean) session.getAttribute("user");
		//取得したログイン会員情報のユーザIDを条件にDBからユーザ情報を取得
		User user = userRepository.getReferenceById(userBean.getId());
		//取得したユーザ情報を注文入力フォーム情報に設定
		orderForm.setId(user.getId());
		orderForm.setPostalCode(user.getPostalCode());
		orderForm.setAddress(user.getAddress());
		orderForm.setName(user.getName());
		orderForm.setPhoneNumber(user.getPhoneNumber());
		//注文入力フォーム情報の支払方法に初期値としてクレジットカードを設定
		orderForm.setPayMethod(1);
		//注文入力フォーム情報をセッションスコープに保存
		session.setAttribute("order", orderForm);
		//届け先入力画面表示処理へリダイレクト
		return "redirect:/client/order/address/input";
	}

	/**
	 * 	届け先入力画面表示処理
	 * @param model ビューとの値受け渡し
	 * @return 登録画面表示
	 */
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.GET)
	public String confirmOrder(Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderForm = (OrderForm) session.getAttribute("order");
		//注文入力フォーム情報をリクエストスコープに設定
		model.addAttribute("orderForm", orderForm);
		//セッションスコープに入力エラー情報がある場合
		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result != null) {
			//取得したエラー情報をリクエストスコープに設定
			model.addAttribute("org.springframework.validation.BindingResult.orderForm", result);
			//セッションスコープから、エラー情報を削除
			session.removeAttribute("result");
		}
		//登録画面表示
		return "client/order/address_input";

	}

	/**
	 * 届け先入力画面 次へボタン 押下時処理
	 * @param order 入力された注文情報のフォーム
	 * @param result 入力チェック
	 * @param model ビューとの値受け渡し
	 * @return 支払方法選択画面表示処理にリダイレクト
	 */
	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.POST)
	public String exeOrder(@Valid @ModelAttribute OrderForm order, BindingResult result, Model model) {
		//画面から入力されたフォーム情報を注文入力フォーム情報として保存
		session.setAttribute("order", order);
		//BindingResultオブジェクトに入力エラー情報がある場合
		if (result.hasErrors()) {
			//入力エラー情報をセッションスコープに設定
			session.setAttribute("result", result);
			//届け先入力画面表示処理にリダイレクト
			return "redirect:/client/order/address/input";
			//入力エラーがない場合
		} else {
			//支払方法選択画面表示処理にリダイレクト
			return "redirect:/client/order/payment/input";
		}
	}

	/**
	 * 支払方法選択画面表示処理
	 * @param order 入力された注文情報のフォーム
	 * @param result 入力チェック
	 * @param model ビューとの値受け渡し
	 * @return 支払方法選択画面表示
	 */
	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.GET)
	public String payment(@ModelAttribute OrderForm order, BindingResult result, Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderForm = (OrderForm) session.getAttribute("order");
		//注文フォーム情報をリクエストスコープに設定
		model.addAttribute("postalCode", orderForm.getPostalCode());
		model.addAttribute("address", orderForm.getAddress());
		model.addAttribute("name", orderForm.getName());
		model.addAttribute("phoneNumber", orderForm.getPhoneNumber());
		model.addAttribute("payMethod", orderForm.getPayMethod());
		//支払方法選択画面表示
		return "client/order/payment_input";
	}

	/**
	 * 
	 * 支払方法選択画面 次へボタン 押下時処理
	 * 
	 * @param order 入力された注文情報のフォーム
	 * @param result 入力チェック
	 * @param payMethod 支払い方法の情報
	 * @return 注文確認画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/check", method = RequestMethod.POST)
	public String check(@ModelAttribute OrderForm order, BindingResult result, Integer payMethod) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderForm = (OrderForm) session.getAttribute("order");
		//画面から入力された支払方法を取得した注文入力フォーム情報に設定
		orderForm.setPayMethod(payMethod);
		//注文入力フォーム情報をセッションスコープに保存
		session.setAttribute("order", orderForm);
		//注文確認画面表示処理へリダイレクト
		return "redirect:/client/order/check";
	}

	/**
	 * 
	 * 注文確認画面表示処理
	 * 
	 * @param order 入力された注文情報のフォーム
	 * @param model ビューとの値受け渡し
	 * @return 注文確認画面表示
	 */
	@RequestMapping(path = "/client/order/check", method = RequestMethod.GET)
	public String check(OrderForm order, Model model) {
		//合計金額
		int total = 0;
		//金額小計
		int Allprice = 0;
		//買い物かごのリスト生成
		ArrayList<BasketBean> basketBean = new ArrayList<>();
		//買い物かごの更新先リスト生成
		ArrayList<BasketBean> newBaskets = new ArrayList<>();
		//セッションスコープから注文情報を取得
		OrderForm orderForm = new OrderForm();
		orderForm = (OrderForm) session.getAttribute("order");
		//Itemのオブジェクト生成
		Item items = new Item();
		//セッションスコープから買い物かご情報を取得
		basketBean = (ArrayList<BasketBean>) session.getAttribute("basketBeans");

		/*
		 * 注文商品の最新情報をDBから取得し、の在庫チェックをする
		 * 買い物かご情報から、商品ごとの金額小計と全額を算出し、注文入力フォーム情報に設定
		 */
		List<OrderItemBean> orderItemBeanList = new ArrayList<OrderItemBean>();
		for (int i = 0; i < basketBean.size(); i++) {
			BasketBean basket = basketBean.get(i);
			items = itemRepository.getReferenceById(basket.getId());
			//値段
			int price = items.getPrice();
			//注文数
			int orderNum = basket.getOrderNum();
			//在庫
			int stock = items.getStock();
			//在庫切れの場合
			if (stock == 0) {
				//注文警告メッセージをリクエストスコープに保存
				model.addAttribute("itemNameListZero");
				//在庫切れの商品は、買い物かごから情報削除
				basketBean.remove(i);
				//金額小計
				Allprice = price * orderNum;
				//在庫不足の場合
			} else if (orderNum > stock && stock != 0) {
				//注文警告メッセージをリクエストスコープに保存
				model.addAttribute("itemNameListLessThan");
				//注文数を在庫数まで減らす
				orderNum = stock;
				//注文数設定
				basket.setOrderNum(orderNum);
				//金額小計
				Allprice = price * orderNum;
				//買い物かごの情報を更新する
				newBaskets.add(basket);
				//在庫十分の場合
			} else {
				//買い物かご情報を更新する
				newBaskets.add(basket);
				//金額小計
				Allprice = price * orderNum;
			}
			//OrderItemBeanのオブジェクト生成
			OrderItemBean orderItemBean = new OrderItemBean();
			//金額小計、注文数を設定
			orderItemBean.setSubtotal(Allprice);
			orderItemBean.setOrderNum(orderNum);
			//itemの情報をコピー
			BeanUtils.copyProperties(items, orderItemBean);
			//リストに情報を追加
			orderItemBeanList.add(orderItemBean);
			//合計金額
			total += Allprice;
			//合計金額をリクエストスコープに保存
			model.addAttribute("total", total);
		}
		//注文商品情報をリストに保存
		model.addAttribute("orderItemBeans", orderItemBeanList);
		//買い物かご情報をセッションスコープに保存
		session.setAttribute("orderItemBeans", newBaskets);
		//注文入力情報をリクエストスコープに保存
		model.addAttribute("orderForm", session.getAttribute("order"));
		//注文確認画面表示
		return "client/order/check";
	}

	/**
	 * 届け先入力画面で、戻るボタン押下処理
	 * @return 買い物かご画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/basket/list", method = RequestMethod.POST)
	public String back() {
		//買い物かご画面表示処理へリダイレクト
		return "redirect:/client/basket/list";
	}

	/**
	 * 支払い方法選択画面で、戻るボタン押下処理
	 * @return 届け先入力画面 表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/payment/back", method = RequestMethod.POST)
	public String pageBack() {
		//届け先入力画面 表示処理へリダイレクト
		return "redirect:/client/order/address/input";
	}

	/**
	 * ご注文の確定ボタン 押下時処理
	 * @param model ビューとの値受け渡し
	 * @return 注文完了画面表示処理にリダイレクト
	 */
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.POST)
	public String complete(Model model) {
		//セッションスコープから注文情報を取得
		OrderForm orderForm = new OrderForm();
		orderForm = (OrderForm) session.getAttribute("order");

		Item item = new Item();
		//合計金額
		int total = 0;

		/*
		 * 注文情報をDBに登録
		 */

		Order order = new Order();
		//注文日を設定
		Date date = new Date(0);
		order.setInsertDate(date);
		//ユーザ情報を設定
		UserBean userBean = new UserBean();
		User loginUser = new User();
		userBean = (UserBean) session.getAttribute("user");
		BeanUtils.copyProperties(userBean, loginUser);
		order.setUser(loginUser);
		//注文情報をコピー
		BeanUtils.copyProperties(orderForm, order, "id");
		//登録
		orderRepository.save(order);

		//セッションスコープから買い物かご情報を取得
		ArrayList<BasketBean> basketBean = new ArrayList<>();
		basketBean = (ArrayList<BasketBean>) session.getAttribute("orderItemBeans");
		//注文商品の在庫チェックをする
		for (int i = 0; i < basketBean.size(); i++) {
			BasketBean basket = basketBean.get(i);
			item = itemRepository.getReferenceById(basket.getId());
			//注文数
			int orderNum = basket.getOrderNum();
			//在庫数
			int stock = item.getStock();
			int price = item.getPrice();
			int Allprice = price * orderNum;
			//合計金額を更新
			total += Allprice;
			//合計金額をリクエストスコープに保存
			model.addAttribute("total", total);
			//在庫切れの場合
			if (stock < orderNum) {
				//注文確認画面表示処理へリダイレクト
				return "redirect:/client/order/check";
			}

			/*
			 * 注文商品情報をDBに登録
			 */
			OrderItem orderItem = new OrderItem();
			//注文商品情報を設定
			orderItem.setId(basket.getId());
			orderItem.setItem(item);
			orderItem.setOrder(order);
			orderItem.setQuantity(orderNum);
			orderItem.setPrice(price);
			//登録
			orderItemRepository.save(orderItem);

			//在庫数を減らしてDBに登録
			stock = stock - orderNum;
			item.setStock(stock);
			itemRepository.save(item);
		}

		//セッションスコープの注文入力フォーム情報と買い物かご情報を削除
		session.removeAttribute("orderForm");
		session.removeAttribute("basketBeans");
		//注文完了画面表示処理にリダイレクト
		return "redirect:/client/order/complete";
	}

	/**
	 * 注文完了画面表示処理
	 * @return 注文完了画面表示
	 */
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.GET)
	public String orderComplete(Model model) {
		//注文完了画面表示
		return "/client/order/complete";
	}

}
