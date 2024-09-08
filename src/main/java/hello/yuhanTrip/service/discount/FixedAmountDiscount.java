package hello.yuhanTrip.service.discount;


public class FixedAmountDiscount implements DiscountStrategy{

    private Double discountAmount;

    public FixedAmountDiscount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }
    @Override
    public Double applyDiscount(Double originalPrice) {
        return originalPrice - discountAmount;
    }
}
