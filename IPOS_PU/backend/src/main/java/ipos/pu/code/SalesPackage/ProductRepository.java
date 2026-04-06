package ipos.pu.code.SalesPackage;

import ipos.pu.code.model.ProductCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductCache, Integer> {
    List<ProductCache> findByItemIdStartingWithAndIsActive(String category, int isActive);
    List<ProductCache> findByIsActive(int isActive);
}