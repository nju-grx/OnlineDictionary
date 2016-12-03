package client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main extends Application {
    // 定义一个stage管理器
    public static StageController stageController = new StageController();

    // 定义一个线程池
    public static ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    public void start(Stage primaryStage) throws Exception{

        // 将所有待用的stage加载到管理器中
        stageController.loadStage("loginView", "login/LoginView.fxml");
        stageController.loadStage("queryView", "query/QueryView.fxml");
        stageController.loadStage("registerView", "register/RegisterView.fxml");
        stageController.loadStage("userView", "user/UserView.fxml");
        stageController.loadStage("msgView", "message/MsgView.fxml");

        // 显示登陆窗口
        stageController.setStage("loginView");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
