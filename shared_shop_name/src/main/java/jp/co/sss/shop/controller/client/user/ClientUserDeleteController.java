package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

/**
 * 会員管理 削除機能(運用管理者、システム管理者)のコントローラクラス
 *
 * @author SystemShared
 * 
 *  TIPS: 一般会員向けの会員削除機能に類似した処理です。
 *  
 */
@Controller
public class ClientUserDeleteController {

	/**
	 * 会員情報 リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * 確認画面　表示処理
	 *
	 * @param model Viewとの値受渡し
	 * @return "admin/user/delete_check" 確認画面 表示
	 */
	@RequestMapping(path = "/client/user/delete/check", method = RequestMethod.POST)
	public String updateInput(Model model) {
		//セッションから入力フォーム取得		
		UserBean userBean = (UserBean) session.getAttribute("user");
		
		if (userBean == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}


		// 入力フォーム情報を画面表示設定
		model.addAttribute("userForm", userBean);
		session.setAttribute("userForm", userBean);

		// 削除確認画面　表示
		return "client/user/delete_check";
	}

	/**
	 * 会員情報削除完了処理
	 *
	 * @return "redirect:/admin/user/delete/complete" 会員情報 削除完了画面へ
	 */
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.POST)
	public String deleteComplete() {

		// セッションから削除対象フォーム情報を取得
		UserBean userBean = (UserBean) session.getAttribute("userForm");

		
		if (userBean == null) {
			// セッション情報がない場合、エラー
			return "redirect:/syserror";
		}
		// 削除対象の会員情報を取得
		User user = userRepository.findByIdAndDeleteFlag(userBean.getId(), Constant.NOT_DELETED);

		if (user == null) {
			// 対象が無い場合、エラー
			return "redirect:/syserror";
		}

		// 削除フラグを立てる
		user.setDeleteFlag(Constant.DELETED);

		// 会員情報を保存
		userRepository.save(user);

		// セッションの削除対象情報を削除
		session.removeAttribute("userForm");

		// 削除完了画面　表示処理
		return "redirect:/client/user/delete/complete";
	}
//
//	/**
//	 * 会員情報削除完了処理
//	 *
//	 * @return "admin/user/delete_complete" 会員情報 削除完了画面へ
//	 */
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.GET)
	public String deleteCompleteFinish() {

		return "client/user/delete_complete";
	}

}
