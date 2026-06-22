package com.example.PRM.util;

import com.example.PRM.dto.request.ProductFilterReq;
import com.example.PRM.entity.Product;
import com.example.PRM.status_enum.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> filterProducts(ProductFilterReq filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getCondition() != null) {
                predicates.add(cb.equal(root.get("condition"), filter.getCondition()));
            }
            if (filter.getSize() != null && !filter.getSize().isBlank()) {
                predicates.add(cb.equal(root.get("size"), filter.getSize()));
            }
            if (filter.getColor() != null && !filter.getColor().isBlank()) {
                predicates.add(cb.equal(root.get("color"), filter.getColor()));
            }
            if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.join("shop").get("address")),
                        "%" + filter.getLocation().toLowerCase() + "%"
                ));
            }
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            predicates.add(cb.equal(root.get("status"), ProductStatus.AVAILABLE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
