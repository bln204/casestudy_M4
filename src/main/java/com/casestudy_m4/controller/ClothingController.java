package com.casestudy_m4.controller;

import com.casestudy_m4.model.*;
import com.casestudy_m4.repository.ICategoryRepository;
import com.casestudy_m4.service.ClothingService;
import com.casestudy_m4.service.OrderService;
import com.casestudy_m4.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ClothingController {
    private static final Logger logger = LoggerFactory.getLogger(ClothingController.class);

    @Autowired
    private ClothingService clothingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ModelAndView home(Model model, @PageableDefault(size = 3) Pageable pageable) {
        logger.debug("Accessing home page with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        // Đảm bảo page không âm
        int pageNumber = Math.max(0, pageable.getPageNumber());
        Pageable adjustedPageable = PageRequest.of(pageNumber, pageable.getPageSize(), pageable.getSort());
        Page<Clothing> clothings = clothingService.findAllWithPaging(adjustedPageable);
        List<Category> categories = categoryRepository.findAll();
        logger.debug("Found {} clothings and {} categories", clothings.getTotalElements(), categories.size());

        // Lấy thông tin người dùng từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("user", authentication.getName());
        } else {
            model.addAttribute("user", null);
        }

        model.addAttribute("clothings", clothings);
        model.addAttribute("categories", categories);
        if (clothings.isEmpty()) {
            model.addAttribute("error", "Hiện tại không có sản phẩm nào trong cửa hàng.");
        }
        return new ModelAndView("index");
    }

    @GetMapping("/search")
    public String search(Model model,
                         @RequestParam(required = false) Double minPrice,
                         @RequestParam(required = false) Double maxPrice,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String size,
                         @PageableDefault(size = 3) Pageable pageable) {
        logger.debug("Searching with minPrice: {}, maxPrice: {}, category: {}, size: {}, page: {}, size: {}",
                minPrice, maxPrice, category, size, pageable.getPageNumber(), pageable.getPageSize());

        // Nếu không có tham số tìm kiếm, chuyển hướng về trang chủ
        if (minPrice == null && maxPrice == null && (category == null || category.trim().isEmpty()) && (size == null || size.trim().isEmpty())) {
            return "redirect:/?page=" + Math.max(0, pageable.getPageNumber()) + "&size=" + pageable.getPageSize();
        }

        // Xử lý tham số rỗng
        if (category != null && category.trim().isEmpty()) {
            category = null;
        }
        if (size != null && size.trim().isEmpty()) {
            size = null;
        }

        // Kiểm tra giá trị không hợp lệ
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

        // Lấy thông tin người dùng từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("user", authentication.getName());
        } else {
            model.addAttribute("user", null);
        }

        // Đảm bảo page không âm
        int pageNumber = Math.max(0, pageable.getPageNumber());
        Pageable adjustedPageable = PageRequest.of(pageNumber, pageable.getPageSize(), pageable.getSort());
        Page<Clothing> clothings = clothingService.searchClothings(minPrice, maxPrice, category, size, adjustedPageable);
        List<Category> categories = categoryRepository.findAll();
        logger.debug("Found {} clothings and {} categories", clothings.getTotalElements(), categories.size());

        // Hiển thị thông báo nếu không tìm thấy sản phẩm
        if (clothings.isEmpty() && (minPrice != null || maxPrice != null || category != null || size != null)) {
            model.addAttribute("error", "Không tìm thấy sản phẩm phù hợp với tiêu chí tìm kiếm.");
        }

        model.addAttribute("clothings", clothings);
        model.addAttribute("categories", categories);
        model.addAttribute("isSearch", true);
        return "index";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long clothingId, @RequestParam int quantity, HttpSession session, Model model) {
        logger.debug("Adding to cart: clothingId={}, quantity={}", clothingId, quantity);
        if (quantity < 1) {
            logger.error("Invalid quantity: {}", quantity);
            model.addAttribute("error", "Số lượng phải lớn hơn 0.");
            model.addAttribute("clothings", clothingService.findAllWithPaging(Pageable.ofSize(4)));
            model.addAttribute("categories", categoryRepository.findAll());
            return "index";
        }
        Optional<Clothing> clothingOpt = clothingService.findById(clothingId);
        if (!clothingOpt.isPresent()) {
            logger.error("Clothing not found for id: {}", clothingId);
            model.addAttribute("error", "Sản phẩm không tồn tại.");
            model.addAttribute("clothings", clothingService.findAllWithPaging(Pageable.ofSize(4)));
            model.addAttribute("categories", categoryRepository.findAll());
            return "index";
        }
        Clothing clothing = clothingOpt.get();
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
        Iterable<Clothing> clothings = clothingService.findAll();
        Map<Long, Clothing> clothingMap = new HashMap<>();
        for (Clothing clothing : clothings) {
            clothingMap.put(clothing.getId(), clothing);
        }
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
        Iterable<Clothing> clothings = clothingService.findAll();
        Map<Long, Clothing> clothingMap = new HashMap<>();
        for (Clothing clothing : clothings) {
            clothingMap.put(clothing.getId(), clothing);
        }
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

    @GetMapping("/login")
    public String showLoginForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/login?signupSuccess=true";
        } catch (Exception e) {
            model.addAttribute("signupError", e.getMessage());
            return "login";
        }
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logger.debug("Processing logout request");
        if (authentication != null && authentication.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            logger.debug("User logged out successfully");
        }
        return "redirect:/login?logout=true";
    }
}
