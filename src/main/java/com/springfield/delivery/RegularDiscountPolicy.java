package com.springfield.delivery;

public class RegularDiscountPolicy implements DiscountPolicy {

    @Override
    public double applyDiscount(double currentPrice) {
        return currentPrice * 0.95;
    }
}