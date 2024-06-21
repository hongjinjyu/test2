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
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員管理　登録機能(一般会員)のコントローラクラス
 * @author ジ・ジョンヒョン
 */
@Controller
public class ClientUserRegistController {
	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * usersテーブル用リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * 新規登録リンク クリック時処理（GET）
	 * @return "redirect:/admin/user/regist/input" 入力画面　表示処理
	 */
	@RequestMapping(path = "/client/user/regist/input/init", method = RequestMethod.GET)
	public String registInputInit() {
		//入力情報を新規生成
		UserForm userForm = new UserForm();
		//入力情報をセッションスコープに保存
		session.setAttribute("userForm", userForm);
		return "redirect:/client/user/regist/input";
	}

	/**
	 * 入力画面　表示処理(GET)
	 * 
	 * @param model Viewとの値受渡し
	 * @return "client/user/regist_input" 入力画面　表示
	 */
	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.GET)
	public String registInput(Model model) {
		//セッションスコープから入力情報フォームを取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");

		//		// セッション情報がない場合、エラー
		//		if (userForm == null) {
		//			return "redirect:/syserror";
		//		}

		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result != null) {
			//セッションにスコープにエラーがある場合、
			//エラー情報をリクエストスコープに設定
			model.addAttribute("org.springframework.validation.BindingResult.userForm", result);
			// セッションスコープからエラー情報を削除
			session.removeAttribute("result");
		}
		// 入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", userForm);
		// 登録画面　表示
		return "client/user/regist_input";
	}

	/**
	 * 確認ボタン 押下時処理
	 *
	 * @param form 入力フォーム
	 * @param result 入力値チェックの結果
	 * @return 
	 * 	入力値エラーあり："redirect:/admin/user/regist/input" 入力録画面　表示処理
	 * 	入力値エラーなし："redirect:/admin/user/regist/check" 登録確認画面　表示処理
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.POST)
	public String registInputCheck(@Valid @ModelAttribute UserForm form, BindingResult result) {
		//セッションスコープからフォーム情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		// 画面から入力された入力フォームを、セッションスコープに入力フォーム情報として保存
		session.setAttribute("userForm", form);
		// セッション情報が無い場合、エラー
		if (userForm == null) {
			return "redirect:/syserror";
		}
		if (result.hasErrors()) {
			// 入力エラー情報と入力フォーム情報を設定
			session.setAttribute("result", result);
			//  登録入力画面表示処理にリダイレクト
			return "redirect:/client/user/regist/input";
		}
		// 登録確認画面表示処理にリダイレクト
		return "redirect:/client/user/regist/check";
	}

	/**
	 * 確認画面　表示処理
	 *
	 * @param model Viewとの値受渡し
	 * @return "client/user/regist_check" 確認画面　表示
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.GET)
	public String registCheck(Model model) {
		//セッションから入力フォーム情報取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		//入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", userForm);
		if (userForm == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}
		//登録確認画面表示
		return "client/user/regist_check";
	}

	/**
	 * 情報登録処理
	 *
	 * @return "redirect:/admin/user/regist/complete" 登録完了画面　表示処理
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.POST)
	public String registComplete() {
		//セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}
		//入力フォーム情報を元にDB登録用エンティティオブジェクトを生成
		User user = new User();
		BeanUtils.copyProperties(userForm, user);
		// DB登録実施
		userRepository.save(user);
		//セッションスコープの入力フォーム情報削除
		session.removeAttribute("userForm");
		
		UserBean userBean = new UserBean();
		BeanUtils.copyProperties(user, userBean);
		session.setAttribute("user", userBean);
		
		//登録完了画面表示処理にリダイレクト
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