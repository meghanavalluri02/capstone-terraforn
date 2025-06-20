package com.business.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.business.basiclogics.Logic;
import com.business.entities.Admin;
import com.business.entities.Orders;
import com.business.entities.Product;
import com.business.entities.User;
import com.business.loginCredentials.AdminLogin;
import com.business.loginCredentials.UserLogin;
import com.business.services.AdminServices;
import com.business.services.OrderServices;
import com.business.services.ProductServices;
import com.business.services.UserServices;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UserServices services;

    @Autowired
    private AdminServices adminServices;

    @Autowired
    private ProductServices productServices;

    @Autowired
    private OrderServices orderServices;

    // Admin login validation
    @GetMapping("/adminLogin")
    public String getAllData(@ModelAttribute("adminLogin") AdminLogin login, Model model) {
        String email = login.getEmail();
        String password = login.getPassword();
        if (adminServices.validateAdminCredentials(email, password)) {
            return "redirect:/admin/services";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "Login";
        }
    }

    // User login
    @GetMapping("/userlogin")
    public String userLogin(@ModelAttribute("userLogin") UserLogin login, Model model, HttpSession session) {
        String email = login.getUserEmail();
        String password = login.getUserPassword();

        if (services.validateLoginCredentials(email, password)) {
            User user = this.services.getUserByEmail(email);
            session.setAttribute("userEmail", email);

            List<Orders> orders = this.orderServices.getOrdersForUser(user);
            model.addAttribute("orders", orders);
            model.addAttribute("name", user.getUname());
            return "BuyProduct";
        } else {
            model.addAttribute("error2", "Invalid email or password");
            return "Login";
        }
    }

    // Product search
    @PostMapping("/product/search")
    public String searchHandler(@RequestParam("productName") String name, Model model, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        User user = services.getUserByEmail(email);

        Product product = this.productServices.getProductByName(name);
        List<Orders> orders = this.orderServices.getOrdersForUser(user);

        if (product == null) {
            model.addAttribute("message", "SORRY...! Product Unavailable");
        }

        model.addAttribute("orders", orders);
        model.addAttribute("product", product);
        return "BuyProduct";
    }

    // Place an order
    @PostMapping("/product/order")
    public String orderHandler(@ModelAttribute Orders order, Model model, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        User user = services.getUserByEmail(email);

        double totalAmount = Logic.countTotal(order.getoPrice(), order.getoQuantity());
        order.setTotalAmount(totalAmount);
        order.setUser(user);
        order.setOrderDate(new Date());

        this.orderServices.saveOrder(order);
        model.addAttribute("amount", totalAmount);
        return "Order_success";
    }

    @GetMapping("/product/back")
    public String back(Model model, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        User user = services.getUserByEmail(email);

        List<Orders> orders = this.orderServices.getOrdersForUser(user);
        model.addAttribute("orders", orders);
        return "BuyProduct";
    }

    // Admin dashboard
    @GetMapping("/admin/services")
    public String returnBack(Model model) {
        List<User> users = this.services.getAllUser();
        List<Admin> admins = this.adminServices.getAll();
        List<Product> products = this.productServices.getAllProducts();
        List<Orders> orders = this.orderServices.getOrders();

        model.addAttribute("users", users);
        model.addAttribute("admins", admins);
        model.addAttribute("products", products);
        model.addAttribute("orders", orders);

        return "Admin_Page";
    }

    @GetMapping("/addAdmin")
    public String addAdminPage() {
        return "Add_Admin";
    }

    @PostMapping("/addingAdmin")
    public String addAdmin(@ModelAttribute Admin admin) {
        this.adminServices.addAdmin(admin);
        return "redirect:/admin/services";
    }

    @GetMapping("/updateAdmin/{adminId}")
    public String update(@PathVariable("adminId") int id, Model model) {
        Admin admin = this.adminServices.getAdmin(id);
        model.addAttribute("admin", admin);
        return "Update_Admin";
    }

    @GetMapping("/updatingAdmin/{id}")
    public String updateAdmin(@ModelAttribute Admin admin, @PathVariable("id") int id) {
        this.adminServices.update(admin, id);
        return "redirect:/admin/services";
    }

    @GetMapping("/deleteAdmin/{id}")
    public String deleteAdmin(@PathVariable("id") int id) {
        this.adminServices.delete(id);
        return "redirect:/admin/services";
    }

    @GetMapping("/addProduct")
    public String addProduct() {
        return "Add_Product";
    }

    @GetMapping("/updateProduct/{productId}")
    public String updateProduct(@PathVariable("productId") int id, Model model) {
        Product product = this.productServices.getProduct(id);
        model.addAttribute("product", product);
        return "Update_Product";
    }

    @GetMapping("/addUser")
    public String addUser() {
        return "Add_User";
    }

    @GetMapping("/updateUser/{userId}")
    public String updateUserPage(@PathVariable("userId") int id, Model model) {
        User user = this.services.getUser(id);
        model.addAttribute("user", user);
        return "Update_User";
    }
}
