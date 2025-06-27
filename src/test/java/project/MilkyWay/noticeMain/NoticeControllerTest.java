package project.MilkyWay.noticeMain;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import project.MilkyWay.Config.TestSecurityConfig;
import project.MilkyWay.Inquire.Controller.InqurieController;

@WebMvcTest(InqurieController.class)
@Import(TestSecurityConfig.class)
public class NoticeControllerTest {
}
