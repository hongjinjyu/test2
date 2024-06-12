package jp.co.sss.shop.controller.client.order;

import java.util.ArrayList;
import java.util.List;

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
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;

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
	//ご注文のお手続きボタン 押下時処理
	@RequestMapping(path ="/client/order/address/input", method =RequestMethod.POST)
	public String inputOrder() {
		OrderForm orderForm=new OrderForm();
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
	//届け先入力画面表示処理
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.GET)
	public String confirmOrder(Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderform = (OrderForm) session.getAttribute("order");
		//注文入力フォーム情報をリクエストスコープに設定
		model.addAttribute("orderForm", orderform);
		//セッションスコープに入力エラー情報がある場合
		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result != null) {
			//取得したエラー情報をリクエストスコープに設定
			model.addAttribute("org.springframework.validation.BindingResult.orderForm", result);
			//セッションスコープから、エラー情報を削除
			session.removeAttribute("result");
		}
		return "client/order/address_input";

	}
	
	//届け先入力画面 次へボタン 押下時処理
	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.POST)
	public String exeOrder(@Valid @ModelAttribute OrderForm order, BindingResult result,Model model) {
		//画面から入力されたフォーム情報を注文入力フォーム情報として保存
		OrderForm orderForm = new OrderForm();
		orderForm.setPostalCode(orderForm.getPostalCode());
		orderForm.setAddress(orderForm.getAddress());
		orderForm.setName(orderForm.getName());
		orderForm.setPhoneNumber(orderForm.getPhoneNumber());
		orderForm.setPayMethod(1);
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
	
	//支払方法選択画面表示処理
	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.GET)
	public String payment(@ModelAttribute OrderForm order, BindingResult result, Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderForm = (OrderForm) session.getAttribute("order");
		//注文フォーム情報をリクエストスコープに設定
		model.addAttribute("postalCode", orderForm.getPostalCode());
		model.addAttribute("address", orderForm.getAddress());
		model.addAttribute("name", orderForm.getName());
		model.addAttribute("phoneNumber", orderForm.getPhoneNumber());
		model.addAttribute("payMethod",orderForm.getPayMethod());
		//支払方法選択画面表示
		return "client/order/payment_input";
	}
	
	//支払方法選択画面 次へボタン 押下時処理
	@RequestMapping(path = "/client/order/check", method = RequestMethod.POST)
	public String check(@ModelAttribute OrderForm order, BindingResult result,Integer payMethod) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderForm = (OrderForm) session.getAttribute("order");
		//画面から入力された支払方法を取得した注文入力フォーム情報に設定
		orderForm.setPayMethod(payMethod);
		//注文入力フォーム情報をセッションスコープに保存
		session.setAttribute("order", orderForm);
		//注文確認画面表示処理へリダイレクト
		return "redirect:/client/order/check";
	}
	
	//注文確認画面表示処理
	@RequestMapping(path = "/client/order/check", method = RequestMethod.GET)
	public String orderCheck(Model model) {
		//セッションスコープから買い物かご情報を取得
		List<BasketBean> basketList = (List<BasketBean>) session.getAttribute("basketBean");
		for(int i=0;i<basketList.size();++i) {
			//注文商品の最新情報をDBから取得し、の在庫チェックをする
			BasketBean basket=basketList.get(i);
			Item item=itemRepository.getReferenceById(basket.getId());
			//在庫数
			int stock=item.getStock();
			//注文数
			int orderNum=basket.getOrderNum();
			//在庫切れの場合
			if(stock==0) {
				//注文警告メッセージをリクエストスコープに保存
				model.addAttribute("エラー");
				//在庫切れの商品は、買い物かごから情報を削除
				session.invalidate();
			}
			//在庫不足の場合
			else if(stock<orderNum && stock!=0) {
				//注文警告メッセージをリクエストスコープに保存
				model.addAttribute("エラー");				
				//注文数を在庫数の数に減らす
				orderNum=stock;
			}
			//在庫状況を反映した買い物かご情報をセッションに保存
			session.setAttribute("newbasket",basketList);
			
			List<Integer> subtotal =new ArrayList<>();
			int total=0;
			
			//買い物かご情報から、商品ごとの金額小計と全額を算出し、注文入力フォーム情報に設定
			List<BasketBean> newbasket = (List<BasketBean>) session.getAttribute("newbasket");
			for(int j=0;i<basketList.size();++j) {
				BasketBean Newbasket=basketList.get(j);
				Item newitem=itemRepository.getReferenceById(Newbasket.getId());
				//値段
				int price=newitem.getPrice();
				//注文数
				int NewOrderNum=Newbasket.getOrderNum();
				//金額小計
				int sum=price*NewOrderNum;
				subtotal.add(sum);
				total=total+subtotal.get(i);
			}
			
			
			
		}
		
		
		return "client/order/check";
	}
	
	//届け先入力画面で、戻るボタン押下処理
	@RequestMapping(path = "/client/basket/list", method = RequestMethod.POST)
	public String back() {
		//買い物かご画面表示処理へリダイレクト
		return "redirect:/client/basket/list";
	}
	
	//支払い方法選択画面で、戻るボタン押下処理
	@RequestMapping(path = "/client/order/payment/back", method = RequestMethod.POST)
	public String pageback(){
		//届け先入力画面 表示処理へリダイレクト
		return "redirect:/client/order/address/input";
	}
	
	//ご注文の確定ボタン 押下時処理
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.POST)
	public String complete(){
		
		
		
		
		return "redirect:/client/order/complete";
	}
	
	//注文完了画面表示処理
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.GET)
	public String ordercomplete(){
		return "/client/order/complete";
	}
	
	

}
