package sample.cafekiosk.spring.api.controller.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sample.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "상품 타입은 필수입니다.")
    private ProductType type;

    @NotNull(message = "상품 판매상태는 필수입니다.")
    private ProductSellingStatus sellingStatus;

    // String name -> 상품 이름은 20자 제한(도메인 정책 정의)
    // Request DTO 에서 막고 싶은 마음이 들지만, Controller Layer에서 검증 책임을 가져야 할 규칙이 아님
    // Controller Layer 에서는 유효한 문자열로서 마땅히 가져야 할 조건 정도만 검증, 최소한의 검증
//    @NotNull // "", "  " 통과
//    @NotEmpty // "  " 통과, ""는 실패
    @NotBlank(message = "상품 이름은 필수입니다.") // "  ", "", null 모두 실패
    private String name;

    @Positive(message = "상품 가격은 양수여야 합니다.")
    private int price;

    @Builder
    private ProductCreateRequest(ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        this.type = type;
        this.sellingStatus = sellingStatus;
        this.name = name;
        this.price = price;
    }

    public ProductCreateServiceRequest toServiceRequest() {
        return ProductCreateServiceRequest.builder()
                .name(name)
                .type(type)
                .price(price)
                .sellingStatus(sellingStatus)
                .build();
    }
}
