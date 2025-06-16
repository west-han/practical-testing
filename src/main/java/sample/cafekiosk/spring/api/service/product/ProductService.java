package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;

import java.util.List;
/**
 * readOnly = true : 읽기전용
 * CRUD 에서 CUD 동작 X / only Read
 * JPA : CUD 스냅샷 저장, 변경 감지 기능 실행 X (성능 향상)
 *
 * CQRS - Command(CUD) / Query(Read) 분리
 * 일반적으로 Read 작업이 다른 작업에 비해 압도적으로 많음
 * -> Command, Query가 서로 관련이 없게끔 하자.
 *    시스템 부하로 Query 때문에 장애가 생겼는데 Command가 동작하지 않거나,
 *    Command 때문에 장애가 생겼는데 Query가 동작하지 않는 문제가 발생할 수 있음.
 *    가장 간단한 방법으로 readOnly = true 를 이용해 쿼리 전용 서비스와 커맨드 전용 서비스를 분리할 수 있음.
 *    장점 중 하나는 DB end-point를 분리할 수 있다는 것.
 *    Read 전용 DB, Write DB. Master(Writer)-Slave(Reader) 구조로 DB 서버 구성하고, readOnly 옵션에 따라 엔드포인트를 달리 하는 방법
 *    AWS Aurora DB cluster mode 등에서는 자동으로 구분하여 처리해주기도 하고, Spring에서 엔드포인트를 다르게 처리할 수도 있음
 *
 * 아래와 같이 서비스에 readOnly=true를 기본적으로 설정하고, CUD 작업이 필요한 메소드에만 별도로 @Transactional을 적용해 readOnly=false로 설정
 * 조금 더 나아가면, 아예 조회 전용 서비스와 커맨드 전용 서비스를 분리해서 관리하는 방법 도입
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    // 동시성 이슈 고려 필요
    // 동시성 문제 발생 가능성이 낮다 -> n회 재시도 로직 구현
    // 동시성 문제 발생 가능성이 높다 -> 이전 값으로부터 증가하는 값 대신 UUID 등 상품번호에 대한 정책 변경
    // 등등 동시성 문제를 해결하기 위해 고려할 수 있는 다양한 방법이 있음
    @Transactional // readOnly = false
    public ProductResponse createProduct(ProductCreateServiceRequest request) {
        String nextProductNumber = createNextProductNumber();

        Product product = request.toEntity(nextProductNumber);

        Product savedProduct = productRepository.save(product);

        return ProductResponse.of(savedProduct);
    }

    public List<ProductResponse> getSellingProducts() {
        List<Product> products = productRepository.findAllBySellingStatusIn(ProductSellingStatus.forDisplay());

        return products.stream()
                .map(ProductResponse::of)
                .toList();
    }

    private String createNextProductNumber() {
        String latestProductNumber = productRepository.findLatestProductNumber();

        if (latestProductNumber == null) {
            return "001";
        }

        int latestProductNumberInt = Integer.parseInt(latestProductNumber);
        int nextProductNumberInt = latestProductNumberInt + 1;

        return String.format("%03d", nextProductNumberInt);
    }
}
