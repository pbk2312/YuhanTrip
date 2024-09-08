package hello.yuhanTrip.service.discount;


public class PercentageDiscount implements DiscountStrategy{

    private Double discountRate;  // 할인 비율 (예: 0.2는 20% 할인)

    public PercentageDiscount(Double discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public Double applyDiscount(Double originalPrice) {
        return originalPrice - (originalPrice * discountRate);  // 비율 할인 적용
    }
}
