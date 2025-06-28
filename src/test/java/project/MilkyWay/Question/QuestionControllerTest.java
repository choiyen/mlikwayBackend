package project.MilkyWay.Question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.TestSecurityConfig;
import project.MilkyWay.Inquire.Controller.InqurieController;
import project.MilkyWay.Login.Service.UserService;
import project.MilkyWay.Question.Service.QuestionsService;

@WebMvcTest(InqurieController.class)
@Import(TestSecurityConfig.class)
public class QuestionControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionsService questionsService;

    @MockBean
    LoginSuccess loginSuccess;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

}
