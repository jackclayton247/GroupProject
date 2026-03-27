package ipos.pu.code.SalesPackage.catalogue;

import ipos.pu.code.SalesPackage.catalogue.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByItemIdStartingWithAndIsActive(String category, int isActive);
    List<Product> findByIsActive(int isActive);
}