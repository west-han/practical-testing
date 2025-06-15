package sample.cafekiosk.spring.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * select *
     * from product
     * where selling_status in ('SELLING', 'HOLD');
     *
     * 단순한 쿼리는 테스트가 필요없을 수도 있지만,
     * - 긴 쿼리
     * - 파라미터를 잘못 준 경우
     * - 쿼리 메소드가 아닌 JPQL, QueryDSL, MyBatis 등을 사용하는 경우
     * - 또는 구현하는 기술이나 방법이 변경되는 경우(미래에 어떤 형태로 변할지 모름)
     * 등의 상황에서 조회 쿼리를 날리는 동작의 일관성을 보장하기 위해 테스트를 작성해야 함.
     *
     * Repository 테스트는 사실상 단위 테스트에 가까운 성격의 테스트
     * 테스트를 위해 서버를 띄우지만, 레이어별로 끊어서 봤을 때 영속성 계층은 데이터베이스에 액세스 하는 로직만 가지고 있기 때문
     * 따라서 기능 단위로 보면 단위 테스트의 성격을 가짐
     */
    List<Product> findAllBySellingStatusIn(List<ProductSellingStatus> sellingStatuses);

    List<Product> findAllByProductNumberIn(List<String> productNumbers);

    @Query(value = "select p.product_number from product p order by p.id desc limit 1", nativeQuery = true)
    String findLatestProductNumber();
}
