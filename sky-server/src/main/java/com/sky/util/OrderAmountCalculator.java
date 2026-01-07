package com.sky.util;

import com.sky.exception.ShoppingCartBusinessException;
import com.sky.readmodel.ShoppingCartRM;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.sky.constant.MessageConstant.DISH_ID_AND_SETMEAL_ID_CANNOT_BOTH_BE_NULL;

public final class OrderAmountCalculator {

    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(6); // 配送费 2 元
    private static final BigDecimal PACK_FEE_UNIT = BigDecimal.valueOf(1); // 打包费单价 1 元

    private OrderAmountCalculator() {
    }

    /**
     * 计算订单总金额 = 商品金额 + 打包费 + 配送费
     *
     * @param cartItems 购物车条目（amount 视为单价）
     * @return 总金额（保留 2 位小数）
     */
    public static BigDecimal calcTotalAmount(List<ShoppingCartRM> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("cartItems is empty");
        }

        BigDecimal goodsTotal = BigDecimal.ZERO;
        int setmealQty = 0; // 套餐份数（按 number 累加）
        int dishQty = 0; // 菜品份数（按 number 累加）

        for (ShoppingCartRM item : cartItems) {
            if (item == null)
                continue;

            Integer number = item.getNumber();
            BigDecimal unitPrice = item.getAmount();

            if (number == null || number <= 0) {
                throw new IllegalArgumentException("invalid number: " + number);
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("invalid amount: " + unitPrice);
            }

            // 1) 商品金额：单价 * 数量
            goodsTotal = goodsTotal.add(unitPrice.multiply(BigDecimal.valueOf(number)));

            // 2) 打包费计数
            if (item.getSetmealId() != null) {
                setmealQty += number; // 套餐按份数
            } else if (item.getDishId() != null) {
                dishQty += number; // 菜品按份数（默认）
            } else {
                // dishId 和 setmealId 都为空属于脏数据
                throw new ShoppingCartBusinessException(DISH_ID_AND_SETMEAL_ID_CANNOT_BOTH_BE_NULL);
            }
        }

        // 套餐打包费：每份 1 元
        BigDecimal setmealPackFee = PACK_FEE_UNIT.multiply(BigDecimal.valueOf(setmealQty));

        // 菜品打包费（默认：每份 1 元）
        BigDecimal dishPackFee = PACK_FEE_UNIT.multiply(BigDecimal.valueOf(dishQty));

        BigDecimal packFee = setmealPackFee.add(dishPackFee);

        BigDecimal total = goodsTotal.add(packFee).add(DELIVERY_FEE);

        // 金额统一保留两位
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
