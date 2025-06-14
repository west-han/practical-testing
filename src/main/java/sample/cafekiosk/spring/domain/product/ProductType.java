package sample.cafekiosk.spring.domain.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ProductType {

    HANDMADE("제조 음료"),
    BOTTLE("병 음료"),
    BAKERY("베이커리");

    private final String text;

    private static final List<ProductType> STOCK_TYPES = List.of(BOTTLE, BAKERY);

    // 재고를 관리하는 유형이 현재는 BOTTLE, BAKERY 뿐이지만, 언제 변할지 모름 -> 테스트 코드로 대비
    public static boolean containsStockType(ProductType type) {
        return STOCK_TYPES.contains(type);
    }
}
