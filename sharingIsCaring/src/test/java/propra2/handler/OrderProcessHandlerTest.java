package propra2.handler;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import propra2.database.OrderProcess;
import propra2.model.OrderProcessStatus;
import propra2.repositories.OrderProcessRepository;

import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderProcessHandlerTest {

    private OrderProcessHandler orderProcessHandler;

    @Autowired
    OrderProcessRepository orderProcessRepository;

    public OrderProcessHandlerTest() {

        orderProcessHandler = new OrderProcessHandler();
    }

    @Test
    public void updateOrderProcessTest(){
        OrderProcess orderProcess = new OrderProcess();
        orderProcess.setOwnerId(5678L);
        orderProcess.setRequestId(3456L);
        orderProcess.setStatus(OrderProcessStatus.PENDING);

        orderProcess = orderProcessRepository.save(orderProcess);

        orderProcess.setStatus(OrderProcessStatus.DENIED);

        orderProcessHandler.updateOrderProcess(orderProcess, orderProcessRepository);

        Optional<OrderProcess> expectedOrderProcess = orderProcessRepository.findById(orderProcess.getId());

        Assertions.assertThat(expectedOrderProcess.get().getStatus().toString()).isEqualTo("DENIED");
    }



}
