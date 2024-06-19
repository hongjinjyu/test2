package jp.co.sss.shop.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.repository.FavoriteRepository;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    public List<Favorite> getAllFavorites() {
        // JpaRepository의 findAll() 메서드를 사용하여 모든 Favorite 엔티티를 가져옵니다.
        List<Favorite> favorites = favoriteRepository.findAll();
        return new ArrayList<>(favorites); // 가져온 데이터를 ArrayList로 반환합니다.
    }
}
