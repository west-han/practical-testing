package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.response.OrderResponse;
import sample.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 현재의 패키지 구조 상 API 하위에 각 계층이 controller, service 로 분리되어 있음
 * 기존에는 서비스 레이어에서 controller 패키지의 Request DTO 에 의존
 * 추후 서비스가 커지고, 서비스 레이어와 컨트롤러 레이어를 분리하고 싶어 모듈을 분리하거나 하는 식으로 관리할 시에 허들이 될 수 있음
 * 서비스에 있는 컨트롤러의 의존성으로 인한 문제
 * 다른 아키텍처도 마찬가지지만, 레이어드 아키텍처에서 가장 좋은 그림은 하위 레이어가 상위 레이어를 모르는 형태
 * 하위 레이어인 서비스 레이어에서는 컨트롤러 레이어에 대해서 모르는 것이 베스트
 * 이를 구조에 반영하기 위해 서비스용 리퀘스트 DTO 를 따로 만들어서 사용하며, 컨트롤러에서 서비스 메소드 호출 시에 서비스 리퀘스트 DTO 로 변환해서 전달
 *
 * 이렇게 할 경우 또 다른 장점은, 추후 모듈 분리 시에 서비스 레이어에서 별도의 Bean Validation을 하지 않아도 된다는 점
 * Validation 을 위해 Spring Starter Validation 에 대한 의존성을 서비스 레이어에서 가지고 있을 필요가 없다는 장점
 * 클린한 POJO 형태의 DTO 로 관리를 하고, Validation의 책임을 컨트롤러 레이어에 전가 -> 보다 명확한 책임의 분리
 *
 * 서비스가 작을 때는 괜찮지만, 서비스가 커져서 Kiosk, Pos 등 다양한 장비에서 리퀘스트가 들어올 수 있음
 * 이럴 경우, KioskRequestDto, PosRequestDto 등 다양한 컨트롤러 API 에 대해 서비스가 알고 있어야 한다는 부담이 생김
 * 가장 바깥이자 상위의 Presentation Layer 가 변경되더라도 서비스 이하 단이 영향을 받지 않도록, 의존성과 책임 분리를 확실히 해두는 것이 좋음
 */

@RequiredArgsConstructor
@Transactional
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    /**
     * 재고 감소 -> 동시성에 대한 고민이 필요한 대표적인 예시
     * optimistic lock / pessimistic lock / ...
     */
    public OrderResponse createOrder(OrderCreateServiceRequest request, LocalDateTime registeredDateTime) {
        List<String> productNumbers = request.getProductNumbers();

        List<Product> products = findProductsBy(productNumbers);

        deductStockQuantities(products);

        Order order = Order.create(products, registeredDateTime);
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.of(savedOrder);
    }

    private List<Product> findProductsBy(List<String> productNumbers) {
        List<Product> products = productRepository.findAllByProductNumberIn(productNumbers);
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductNumber, p -> p));

        return productNumbers.stream()
                .map(productMap::get)
                .toList();
    }

    private void deductStockQuantities(List<Product> products) {
        List<String> stockProductNumbers = extractStockProductNumbers(products);

        Map<String, Stock> stockMap = createStockMapBy(stockProductNumbers);

        Map<String, Long> productCountingMap = createCountingMapBy(stockProductNumbers);

        for (String stockProductNumber : new HashSet<>(stockProductNumbers)) {
            Stock stock = stockMap.get(stockProductNumber);
            int quantity = productCountingMap.get(stockProductNumber).intValue();

            if (stock.isQuantityLessThan(quantity)) {
                throw new IllegalArgumentException("재고가 부족한 상품이 있습니다.");
            }
            stock.deductQuantity(quantity);
        }
    }
    // map, filter 등 가공 로직은 메소드로 추출해 어떤 작업을 한 것인지 추상화를 해주는 것이 좋음

    private List<String> extractStockProductNumbers(List<Product> products) {
        return products.stream()
                .filter(product -> ProductType.containsStockType(product.getType()))
                .map(Product::getProductNumber)
                .toList();
    }

    private Map<String, Stock> createStockMapBy(List<String> stockProductNumbers) {
        List<Stock> stocks = stockRepository.findAllByProductNumberIn(stockProductNumbers);
        return stocks.stream()
                .collect(Collectors.toMap(Stock::getProductNumber, s -> s));
    }

    private Map<String, Long> createCountingMapBy(List<String> stockProductNumbers) {
        return stockProductNumbers.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
    }

}
