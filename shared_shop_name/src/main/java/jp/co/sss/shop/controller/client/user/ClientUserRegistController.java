package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.form.UserForm;

@Controller
public class ClientUserRegistController {
	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;
	/**
	 * 会員管理　登録機能(運用管理者、システム管理者)のコントローラクラス
	 * 
	 * @author ジ・ジョンヒョン
	 * 
	 * 
	 */
	@RequestMapping(path = "/client/user/regist/input",method = RequestMethod.POST)
	public String registInput() {
		//セッションスコープより入力情報を取り出す。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			userForm = new UserForm();
			userForm.setAuthority(((UserBean) session.getAttribute("user")).getAuthority());

			//空の入力フォーム情報をセッションに保持 登録ボタンからの遷移
			session.setAttribute("userForm", userForm);
		}

		//登録入力画面　表示処理

		return "redirect:/client/user/regist/input";
	}
	/**
	 * 入力画面　表示処理(GET)
	 * 
	 * @param model Viewとの値受渡し
	 * @return "admin/user/regist_input" 入力画面　表示
	 */
	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.GET)
	public String registInput(Model model) {

		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}

		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result != null) {
			//セッションにエラー情報がある場合、エラー情報をスコープに設定
			model.addAttribute("org.springframework.validation.BindingResult.userForm", result);
			// セッションにエラー情報を削除
			session.removeAttribute("result");
		}

		// 入力フォーム情報をスコープに設定
		model.addAttribute("userForm", userForm);

		// 入力画面　表示
		return "client/user/regist_input";
	}
	
	
}