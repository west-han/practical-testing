package sample.cafekiosk.spring.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTypeTest {

    /**
     * 하나의 문단은 하나의 주제를 가져야 한다.
     *
     * 테스트 케이스 내부에 분기문이나 반복문이 들어가는 경우는 곧 여러가지 테스트 케이스를 하나의 테스트에서 검증하고자 한다는 것을 의미한다.
     * 테스트는 작고 명확해야 하는데, 논리적인 구조가 들어가면 테스트의 대상, 목적과 어떠한 테스트의 환경을 구성하고자 하는지 명확히 이해하기 힘들다.
     * 따라서 아래와 같은 코드는 지양해야 하며, Case 확장이 필요한 경우 Parameterized Test 를 사용해야 한다.
     */
    @DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
    @Test
    void ContainsStockType_WrongCase() {
        // given
        ProductType[] productTypes = ProductType.values();

        for (ProductType productType : productTypes) {
            if (productType == ProductType.HANDMADE) {
                // when
                boolean result = ProductType.containsStockType(productType);

                // then
                assertThat(result).isFalse();
            }

            if (productType == ProductType.BAKERY || productType == ProductType.BOTTLE) {
                // when
                boolean result = ProductType.containsStockType(productType);

                // then
                assertThat(result).isTrue();
            }
        }
    }

    @DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
    @Test
    void containsStockType() {
        // given
        ProductType givenType = ProductType.HANDMADE;

        // when
        boolean result = ProductType.containsStockType(givenType);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
    @Test
    void containsStockType2() {
        // given
        ProductType givenType = ProductType.BAKERY;

        // when
        boolean result = ProductType.containsStockType(givenType);

        // then
        assertThat(result).isTrue();
    }

}