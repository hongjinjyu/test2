package jp.co.sss.shop.controller.client.user;

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
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class ClientUserRegistController {
	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;
	
	@Autowired
	UserRepository userRepository;
	/**
	 * 会員管理　登録機能(運用管理者、システム管理者)のコントローラクラス
	 * 
	 * @author ジ・ジョンヒョン
	 * 
	 * 
	 */
	@RequestMapping(path = "/client/user/regist/input/init", method = RequestMethod.GET)
	public String registInput() {

		//セッションスコープより入力情報を取り出す
		UserForm userForm = new UserForm();
//		userForm.setAuthority(2);
		session.setAttribute("userForm", userForm);

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
	
	/**
	 * 登録入力確認　処理
	 *
	 * @param form 入力フォーム
	 * @param result 入力値チェックの結果
	 * @return 
	 * 	入力値エラーあり："redirect:/admin/user/regist/input" 入力録画面　表示処理
	 * 	入力値エラーなし："redirect:/admin/user/regist/check" 登録確認画面　表示処理
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.POST)
	public String registInputCheck(@Valid @ModelAttribute UserForm form, BindingResult result) {

		//直前のセッション情報を取得
		UserForm lastUserForm = (UserForm) session.getAttribute("userForm");
		if (lastUserForm == null) {
			// セッション情報が無い場合、エラー
			return "redirect:/syserror";
		}
		if (form.getAuthority() == null) {
			//権限情報がない場合、セッション情報から値をセット
			form.setAuthority(lastUserForm.getAuthority());
		}

		// 入力フォーム情報をセッションに保持
		session.setAttribute("userForm", form);

		if (result.hasErrors()) {
			// 入力値にエラーがあった場合、エラー情報をセッションに保持
			session.setAttribute("result", result);
			// 登録入力画面　表示処理
			return "redirect:/client/user/regist/input";
		}

		// 登録確認画面　表示処理 
		return "redirect:/client/user/regist/check";
	}

	/**
	 * 確認画面　表示処理
	 *
	 * @param model Viewとの値受渡し
	 * @return "admin/user/regist_check" 確認画面　表示
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.GET)
	public String registCheck(Model model) {
		//セッションから入力フォーム情報取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}
		//入力フォーム情報をスコープへ設定
		model.addAttribute("userForm", userForm);

		//登録確認画面　表示処理
		return "client/user/regist_check";

	}
	

	/**
	 * 情報登録処理
	 *
	 * @return "redirect:/admin/user/regist/complete" 登録完了画面　表示処理
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.POST)
	public String registComplete() {

		//セッション保持情報から入力値再取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}

		// 会員情報を生成
		User user = new User();

		// 入力フォーム情報をエンティティに設定
		BeanUtils.copyProperties(userForm, user);

		// DB登録
		userRepository.save(user);

		//セッションから入力情報削除
		session.removeAttribute("userForm");

		//登録完了画面　表示処理
		//二重送信防止のためリダイレクトを行う
		return "redirect:/client/user/regist/complete";
	}

	/**
	 * 登録完了画面　表示処理
	 *
	 * @return "admin/user/regist_complete" 登録完了画面　表示
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.GET)
	public String registCompleteFinish() {

		return "client/user/regist_complete";
	}

	
	
}