package com.sg.nusiss.shopping.service.discount;


import com.sg.nusiss.shopping.entity.shopping.Game;

import java.math.BigDecimal;

public class NoDiscountStrategy implements IDiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Game game, BigDecimal originalPrice) {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(Game game) {
        return true; // 默认适用于所有游戏
    }

    @Override
    public String getStrategyName() {
        return "NO_DISCOUNT";
    }
}
