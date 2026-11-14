package com.springfield.delivery;

public class VipDiscountPolicy implements DiscountPolicy {

    @Override
    public double applyDiscount(double currentPrice) {
        return currentPrice * 0.85;
    }
}