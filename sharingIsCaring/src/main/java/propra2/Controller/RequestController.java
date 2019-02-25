package propra2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import propra2.database.Customer;
import propra2.database.Notification;
import propra2.database.OrderProcess;
import propra2.database.Product;
import propra2.handler.OrderProcessHandler;
import propra2.model.OrderProcessStatus;
import propra2.repositories.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class RequestController {

    @Autowired
    CustomerRepository customerRepo;

    @Autowired
    OrderProcessRepository orderProcessRepo;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    private OrderProcessHandler orderProcessHandler;

    @GetMapping("/requests")
    public String showRequests(Principal user, final Model model) {
        Long userId = getUserId(user);
        Optional<Customer> customer = customerRepo.findById(userId);

        List<OrderProcess> ownerOrderProcesses = orderProcessRepo.findAllByOwnerId(userId);
        List<OrderProcess> borrowerOrderProcesses = orderProcessRepo.findAllByRequestId(userId);
        model.addAttribute("user", customer.get());
        model.addAttribute("ownerOrderProcesses", ownerOrderProcesses);
        model.addAttribute("borrower", borrowerOrderProcesses);
        boolean admin = false;
        if(customer.get().getRole().equals("ADMIN")){
            admin = true;
        }
        model.addAttribute("admin", admin);
        return "requests";
    }

    private Long getUserId(Principal user) {
        String username = user.getName();
        Long id = customerRepo.findByUsername(username).get().getCustomerId();
        //Optional<Customer> customer = customerRepo.findByUsername(username);
        //Long id = customer.get().getCustomerId();
        return id;
    }

    @GetMapping("/requests/detailsBorrower/{processId}")
    public String showRequestBorrowerDetails(@PathVariable Long processId, Principal user, final Model model) {
        Long userId = getUserId(user);
        Customer customer = customerRepo.findById(userId).get();

        Optional<OrderProcess> process = orderProcessRepo.findById(processId);
        Product product = process.get().getProduct();

        Long ownerId = process.get().getOwnerId();
        Customer owner = customerRepo.findById(ownerId).get();

        model.addAttribute("owner", owner);
        model.addAttribute("product", product);
        model.addAttribute("process", process.get());
        model.addAttribute("user", customer);
        boolean admin = false;
        if(customer.getRole().equals("ADMIN")){
            admin = true;
        }
        model.addAttribute("admin", admin);
        return "requestDetailsBorrower";
    }

    @RequestMapping(value="/requests/detailsBorrower/{processId}", method= RequestMethod.POST, params="action=delete")
    public String deleteByBorrower(@PathVariable Long processId) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcessRepo.delete(orderProcess);

        return "redirect:/requests";
    }

    @RequestMapping(value="/requests/detailsBorrower/{processId}", method=RequestMethod.POST, params="action=return")
    public String returnProduct(@PathVariable Long processId, Principal user) {
        Customer customer = customerRepo.findByUsername(user.getName()).get();
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcess.setStatus(OrderProcessStatus.RETURNED);
        orderProcess.setToDate(new java.sql.Date(System.currentTimeMillis()));
        orderProcessRepo.save(orderProcess);

        Optional<Notification> notification = notificationRepository.findByProcessId(processId);
        if(notification.isPresent()) {
            notificationRepository.delete(notification.get());
        }

        Product product = productRepo.findById(orderProcess.getProduct().getId()).get();
        product.setAvailable(true);
        productRepo.save(product);

        orderProcessHandler.payDailyFee(orderProcess);

        return "redirect:/requests";
    }

    @GetMapping("/requests/detailsOwner/{processId}")
    public String showRequestOwnerDetails(@PathVariable Long processId, Principal user, final Model model) {
        Long userId = getUserId(user);
        Optional<Customer> customer = customerRepo.findById(userId);
        Optional<OrderProcess> process = orderProcessRepo.findById(processId);


        model.addAttribute("user", customer);
        model.addAttribute("product", process.get().getProduct());
        model.addAttribute("process", process.get());
        model.addAttribute("borrower", customerRepo.findById(process.get().getRequestId()).get());
        boolean admin = false;
        if(customer.get().getRole().equals("ADMIN")){
            admin = true;
        }
        model.addAttribute("admin", admin);
        return "requestDetailsOwner";
    }

    @RequestMapping(value="/requests/detailsOwner/{processId}", method=RequestMethod.POST, params="action=acceptProcess")
    public String accept(String message, @PathVariable Long processId) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcess.setStatus(OrderProcessStatus.ACCEPTED);
        ArrayList<String> oldMessages = orderProcess.getMessages();
        ArrayList<String> messages = new ArrayList<>();
        messages.add(message);
        orderProcess.setMessages(messages);

        Product product = productRepo.findById(orderProcess.getProduct().getId()).get();
        product.setAvailable(false);
        product.setBorrowedUntil(orderProcess.getToDate());

        productRepo.save(product);


        orderProcessHandler.updateOrderProcess(oldMessages, orderProcess);

        return "redirect:/requests";
    }

    @RequestMapping(value="/requests/detailsOwner/{processId}", method=RequestMethod.POST, params="action=acceptReturn")
    public String finishProcess(@PathVariable Long processId) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcess.setStatus(OrderProcessStatus.FINISHED);

        orderProcessHandler.updateOrderProcess(orderProcess.getMessages(), orderProcess);

        return "redirect:/requests";
    }

    @RequestMapping(value="/requests/detailsOwner/{processId}", method=RequestMethod.POST, params="action=appeal")
    public String appealProcess(@PathVariable Long processId, String message) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcess.setStatus(OrderProcessStatus.CONFLICT);
        ArrayList<String> oldMessages = orderProcess.getMessages();
        ArrayList<String> messages = new ArrayList<>();
        messages.add(message);
        orderProcess.setMessages(messages);

        System.out.println(message);
        orderProcessHandler.updateOrderProcess(oldMessages, orderProcess);

        return "redirect:/requests";
    }

    @RequestMapping(value="/requests/detailsOwner/{processId}", method=RequestMethod.POST, params="action=deleteProcess")
    public String deleteByOwner(@PathVariable Long processId) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcessRepo.delete(orderProcess);

        return "redirect:/requests";
    }

    @RequestMapping(value="/requests/detailsOwner/{processId}", method=RequestMethod.POST, params="action=deny")
    public String deny(String message, @PathVariable Long processId) {
        OrderProcess orderProcess = orderProcessRepo.findById(processId).get();
        orderProcess.setStatus(OrderProcessStatus.DENIED);
        ArrayList<String> oldMessages = orderProcess.getMessages();
        ArrayList<String> messages = new ArrayList<>();
        messages.add(message);
        orderProcess.setMessages(messages);

        orderProcessHandler.updateOrderProcess(oldMessages, orderProcess);

        return "redirect:/requests";
    }
}
