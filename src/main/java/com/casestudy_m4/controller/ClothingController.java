package com.casestudy_m4.controller;

import com.casestudy_m4.model.CartItem;
import com.casestudy_m4.model.Category;
import com.casestudy_m4.model.Clothing;
import com.casestudy_m4.model.Order;
import com.casestudy_m4.repository.ICategoryRepository;
import com.casestudy_m4.service.ClothingService;
import com.casestudy_m4.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ClothingController {
    private static final Logger logger = LoggerFactory.getLogger(ClothingController.class);

    @Autowired
    private ClothingService clothingService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private ICategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(Model model) {
        logger.debug("Accessing home page");
        List<Clothing> clothings = clothingService.getAllClothings();
        List<Category> categories = categoryRepository.findAll();
        logger.debug("Found {} clothings and {} categories", clothings.size(), categories.size());
        model.addAttribute("clothings", clothings);
        model.addAttribute("categories", categories);
        if (clothings.isEmpty()) {
            model.addAttribute("error", "Hiện tại không có sản phẩm nào trong cửa hàng.");
        }
        return "index";
    }

    @GetMapping("/search")
    public String search(Model model,
                         @RequestParam(required = false) Double minPrice,
                         @RequestParam(required = false) Double maxPrice,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String size) {
        logger.debug("Searching with minPrice: {}, maxPrice: {}, category: {}, size: {}",
                minPrice, maxPrice, category, size);

        // Kiểm tra tham số đầu vào
        if (minPrice != null && minPrice < 0) {
            model.addAttribute("error", "Giá tối thiểu không được âm.");
            minPrice = null;
        }
        if (maxPrice != null && maxPrice < 0) {
            model.addAttribute("error", "Giá tối đa không được âm.");
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            model.addAttribute("error", "Giá tối thiểu không được lớn hơn giá tối đa.");
            minPrice = null;
            maxPrice = null;
        }

        List<Clothing> clothings = clothingService.searchClothings(minPrice, maxPrice, category, size);
        List<Category> categories = categoryRepository.findAll();
        logger.debug("Found {} clothings and {} categories", clothings.size(), categories.size());

        if (clothings.isEmpty() && (minPrice != null || maxPrice != null || category != null || size != null)) {
            model.addAttribute("error", "Không tìm thấy sản phẩm phù hợp với tiêu chí tìm kiếm.");
        }

        model.addAttribute("clothings", clothings);
        model.addAttribute("categories", categories);
        return "index";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long clothingId, @RequestParam int quantity, HttpSession session, Model model) {
        logger.debug("Adding to cart: clothingId={}, quantity={}", clothingId, quantity);
        if (quantity < 1) {
            logger.error("Invalid quantity: {}", quantity);
            model.addAttribute("error", "Số lượng phải lớn hơn 0.");
            List<Clothing> clothings = clothingService.getAllClothings();
            model.addAttribute("clothings", clothings);
            return "index";
        }
        Clothing clothing = clothingService.getClothingById(clothingId);
        if (clothing == null) {
            logger.error("Clothing not found for id: {}", clothingId);
            model.addAttribute("error", "Sản phẩm không tồn tại.");
            List<Clothing> clothings = clothingService.getAllClothings();
            model.addAttribute("clothings", clothings);
            return "index";
        }
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        cart.add(new CartItem(clothingId, clothing.getName(), clothing.getPrice(), quantity));
        session.setAttribute("cart", cart);
        logger.debug("Cart updated, size: {}", cart.size());
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        logger.debug("Accessing cart, size: {}", cart != null ? cart.size() : 0);
        List<Clothing> clothings = clothingService.getAllClothings();
        Map<Long, Clothing> clothingMap = clothings.stream().collect(Collectors.toMap(Clothing::getId, clothing -> clothing));
        double totalAmount = cart != null ? cart.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum() : 0.0;
        model.addAttribute("cart", cart != null ? cart : new ArrayList<>());
        model.addAttribute("clothings", clothingMap);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("order", new Order());
        logger.debug("Total amount for cart: {}", totalAmount);
        return "cart";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute Order order, BindingResult result, HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        logger.debug("Processing checkout, cart size: {}", cart != null ? cart.size() : 0);
        List<Clothing> clothings = clothingService.getAllClothings();
        Map<Long, Clothing> clothingMap = clothings.stream().collect(Collectors.toMap(Clothing::getId, clothing -> clothing));
        double totalAmount = cart != null ? cart.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum() : 0.0;
        if (result.hasErrors() || cart == null || cart.isEmpty()) {
            logger.warn("Checkout failed: validation errors or empty cart");
            model.addAttribute("cart", cart != null ? cart : new ArrayList<>());
            model.addAttribute("clothings", clothingMap);
            model.addAttribute("totalAmount", totalAmount);
            return "cart";
        }
        order.setItems(cart);
        order.setTotalAmount(totalAmount);
        orderService.saveOrder(order);
        logger.debug("Order saved, id: {}", order.getId());
        session.removeAttribute("cart");
        return "redirect:/order";
    }

    @GetMapping("/order")
    public String orderConfirmation() {
        logger.debug("Accessing order confirmation page");
        return "order";
    }
}