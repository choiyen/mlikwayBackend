package project.MilkyWay.BoardMain.Comment;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import project.MilkyWay.BoardMain.Board.Controller.BoardController;
import project.MilkyWay.BoardMain.Comment.Controller.CommentController;
import project.MilkyWay.Config.TestSecurityConfig;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
public class CommentControllerTest {
}
