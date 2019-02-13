package propra2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import propra2.handler.OrderProcessHandler;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import propra2.Security.CustomerValidator;

import propra2.database.Customer;
import propra2.database.OrderProcess;
import propra2.handler.UserHandler;
import propra2.model.UserRegistration;
import propra2.repositories.CustomerRepository;
import propra2.database.Product;
import propra2.repositories.OrderProcessRepository;
import propra2.repositories.ProductRepository;
import java.util.List;

import java.util.Optional;

@Controller
public class SharingIsCaringController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderProcessRepository orderProcessRepository;

    private OrderProcessHandler orderProcessHandler;
    private UserHandler userHandler;

    @Autowired
    private CustomerValidator customerValidator;

    public SharingIsCaringController() {
        orderProcessHandler = new OrderProcessHandler();
        userHandler = new UserHandler();
    }


    @GetMapping("/home")
    public String home(Customer customer, Model model){
        model.addAttribute("user", customer);
        return "home";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String createUser(UserRegistration user, BindingResult bindingResult, Model model) {
        customerValidator.validate(user, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        Customer customer = new Customer();
        customer.setMail(user.getEmailAddress());
        customer.setUsername(user.getUserName());

        customer.setProPay(userHandler.getProPayAccount(user.getUserName()));

        customerRepository.save(customer);
        return "login";
    }

    @GetMapping("/product")
    public String showCreatePerson() {
        return "addProduct";
    }

    @PostMapping("/product")
    public String createProduct(final Product newProduct) {
        if (newProduct.allValuesSet()) {
            productRepository.save(newProduct);
        }
        return "redirect:http://localhost:8080/";
    }

    @PostMapping("/product/{name}")
    List<Product> searchForProducts(String name) {
        List<Product> resultList = productRepository.findByTitle(name);
        return resultList;
    }

    @PostMapping("/product/{id}")
    Product getProductInformationById(Long id) {
        return productRepository.findById(id).get();
    }


    @PostMapping("/")
    public boolean userExists(final String username) {
        if (customerRepository.findByUsername(username).isPresent())
            return true;
        throw new IllegalArgumentException();
    }

    @GetMapping("/profile/{customerId}")
    public String getUserDataById(@PathVariable Long customerId, Model model) {
        Optional<Customer> user = customerRepository.findById(customerId);
        model.addAttribute("user", user.get());
        return "profile";
    }

    @GetMapping("/profile/change/{customerId}")
    public String getUpdateUserData(@PathVariable Long customerId, Model model){
        Optional<Customer> customer = customerRepository.findById(customerId);
        model.addAttribute("user", customer.get());
        return "profileUpdate";
    }

    @PostMapping("/profile/change/{customerId}")
    public String updateUserData(@PathVariable Long customerId, Customer customer, Model model) {
        customerRepository.save(customer);
        model.addAttribute("user", customer);
        return "profile";
    }
  
    @PostMapping("/orderProcess/{id}")
    public void updateOrderProcess(@PathVariable Long id, @RequestBody OrderProcess orderProcess){
        orderProcessHandler.updateOrderProcess(orderProcess, orderProcessRepository);
    }
  
    @GetMapping("/profile/offers/{customerId}")
    public List<Product> getOffers(@PathVariable Long customerId) {
        List<Product> products = productRepository.findByOwnerId(customerId);
        return products;
    }

    @GetMapping("/profile/orders/{customerId}")
    public List<Product> getOrders(@PathVariable Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        List<Product> products = productRepository.findAllById(customer.get().getBorrowedProductIds());
        return products;
    }

    @PostMapping("/orderProcess")
    public String addOrderProcess(OrderProcess newOrderProcess){
        if(newOrderProcess.allValuesSet()) {
            orderProcessRepository.save(newOrderProcess);
        }
        return "";
    }
}
