package jp.co.sss.shop.controller.client.order;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
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
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.POST)
	public String inputorder() {
		//注文入力フォーム情報を作成
		OrderForm orderform = new OrderForm();
		//セッションスコープからログイン会員情報を取得
		UserBean userBean = (UserBean) session.getAttribute("user");
		//取得したログイン会員情報のユーザIDを条件にDBからユーザ情報を取得
		User user = userRepository.getReferenceById(userBean.getId());
		//取得したユーザ情報を注文入力フォーム情報に設定
		BeanUtils.copyProperties(user, orderform, "id");
		//注文入力フォーム情報の支払方法に初期値としてクレジットカードを設定
		orderform.setPayMethod(1);
		//注文入力フォーム情報をセッションスコープに保存
		session.setAttribute("order", orderform);
		//届け先入力画面表示処理へリダイレクト
		return " redeirect:/client/order/address/input ";
	}

	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.GET)
	public String confirmorder(@Valid BindingResult result, Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderform = (OrderForm) session.getAttribute("order");
		//注文入力フォーム情報をリクエストスコープに設定
		model.addAttribute("order", orderform);
		//セッションスコープに入力エラー情報がある場合
		if (result.hasErrors()) {
			//取得したエラー情報をリクエストスコープに設定
			model.addAttribute("result", result);
			//セッションスコープから、エラー情報を削除
			session.invalidate();
		}
		return "client/order/address_input";

	}

	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.POST)
	public String exeorder(@Valid OrderForm orderForm, BindingResult result, Model model) {
		//リクエストスコープに画面から入力されたフォーム情報を注文入力フォーム情報として保存
		OrderForm orderform = new OrderForm();
		model.addAttribute("postalCode", orderform.getPostalCode());
		model.addAttribute("address", orderform.getAddress());
		model.addAttribute("name", orderform.getName());
		model.addAttribute("phoneNumber", orderform.getPhoneNumber());
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

	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.GET)
	public String payment(OrderForm orderForm, Model model) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderform = (OrderForm) session.getAttribute("order");
		//注文フォーム情報をリクエストスコープに設定
		model.addAttribute("postalCode", orderform.getPostalCode());
		model.addAttribute("address", orderform.getAddress());
		model.addAttribute("name", orderform.getName());
		model.addAttribute("phoneNumber", orderform.getPhoneNumber());
		//支払方法選択画面表示
		return "client/order/payment_input";
	}

	@RequestMapping(path = "/client/order/check", method = RequestMethod.POST)
	public String check(Integer payMethod) {
		//セッションスコープから注文入力フォーム情報を取得
		OrderForm orderform = (OrderForm) session.getAttribute("order");
		//画面から入力された支払方法を取得した注文入力フォーム情報に設定
		orderform.setPayMethod(payMethod);
		//注文入力フォーム情報をセッションスコープに保存
		session.setAttribute("order", orderform);
		//注文確認画面表示処理へリダイレクト
		return "redirect:/client/order/check";
	}

	@RequestMapping(path = "/client/order/check", method = RequestMethod.GET)
	public String ordercheck() {
		//セッションスコープから注文情報を取得
		
		return null;
	}

}
