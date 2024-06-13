package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員管理 表示機能(一般会員)のコントローラクラス
 *
 * @author ジ・ジョンヒョン
 */
@Controller
public class ClientUserShowController {
	/**
	 * 会員情報　リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * セッション情報
	 */
	@Autowired
	HttpSession session;

	/**
	 * 詳細表示処理
	 *
	 * @param id 表示対象会員ID
	 * @param model Viewとの値受渡し
	 * @return "client/user/detail" 会員詳細表示画面へ
	 * 
	 * TIPS: 一般会員向けの会員詳細表示機能に類似した処理です。
	 */
	@RequestMapping(path = "/client/user/detail", method = { RequestMethod.GET, RequestMethod.POST })
	public String showUser(Model model) {
		// 表示対象の情報を取得
		UserBean userBean = (UserBean) session.getAttribute("user");
		Object user = userRepository.getReferenceById(userBean.getId());
		// 対象が無い場合、エラー
		if (user == null) {
			return "redirect:/syserror";
		}
		// Userエンティティの各フィールドの値をUserBeanにコピー
		BeanUtils.copyProperties(user, userBean);
		// 会員情報をViewに渡す
		model.addAttribute("userBean", userBean);
		//会員登録・変更・削除用のセッションスコープを初期化
		session.removeAttribute("userForm");
		//詳細画面　表示
		return "client/user/detail";
	}
}
