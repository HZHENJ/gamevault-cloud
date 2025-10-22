package com.sg.nusiss.shopping.repository.shopping;

import com.sg.nusiss.shopping.entity.shopping.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
