package client.query;

import client.Main;
import client.ServerAPI;
import client.SomeException;
import client.user.UserController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.peer.CheckboxPeer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryController implements Initializable{
    //搜索结果
    private static HashMap<String, String> results = new LinkedHashMap<>();

    //点赞数
    private static HashMap<String, Integer> favours = new LinkedHashMap<>();

    //该用户对当前单词的点赞情况
    private static HashMap<String, Boolean> userFavour = new LinkedHashMap<>();

    // 单词输入域
    @FXML
    public TextField inputTextField;

    // 搜索结果显示区域
    public TextArea[] resultTextAreas = new TextArea[3];
    @FXML
    public TextArea resultTextArea0;
    @FXML
    public TextArea resultTextArea1;
    @FXML
    public TextArea resultTextArea2;

    //复选框
    public CheckBox[] checkBoxs = new CheckBox[3];
    @FXML
    public CheckBox baiduCheckBox;
    @FXML
    public CheckBox youdaoCheckBox;
    @FXML
    public CheckBox biyingCheckBox;

    //点赞按钮
    @FXML
    public Button favourButton;

    //输入出错时提示
    @FXML
    public Label hintLabel;

    //显示翻译的工具
    public Label[] toolLabels = new Label[3];
    @FXML
    public Label toolLabel0;
    @FXML
    public Label toolLabel1;
    @FXML
    public Label toolLabel2;

    /**
     * stage初始化
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        results.put("baidu", "");
        results.put("youdao", "");
        results.put("biying", "");
        favours.put("baidu", 0);
        favours.put("youdao", 0);
        favours.put("biying", 0);
        userFavour.put("baidu", false);
        userFavour.put("youdao", false);
        userFavour.put("biying", false);
        checkBoxs[0] = baiduCheckBox;
        checkBoxs[1] = youdaoCheckBox;
        checkBoxs[2] = biyingCheckBox;
        resultTextAreas[0] = resultTextArea0;
        resultTextAreas[1] = resultTextArea1;
        resultTextAreas[2] = resultTextArea2;
        toolLabels[0] = toolLabel0;
        toolLabels[1] = toolLabel1;
        toolLabels[2] = toolLabel2;
        toolLabel0.setText("百度");
        toolLabel1.setText("有道");
        toolLabel2.setText("必应");

        for(CheckBox checkBox: checkBoxs)
            checkBox.setSelected(true);
        for(TextArea resultTextArea: resultTextAreas)
            resultTextArea.setWrapText(true);

        // 设置三个复选框的监听器
        for(CheckBox checkBox: checkBoxs)
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> showResult());
    }


    /**
     * 好友按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void userButtonAction(MouseEvent mouseEvent) {
        Main.stageController.setStage("userView", "queryView");
    }


    /**
     * 注销按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void logoutButtonAction(MouseEvent mouseEvent) {
        ServerAPI.userOffline(Main.userName);
        Main.isOnline = false;
        Main.userName = "";
        Main.stageController.setStage("loginView", "queryView");
    }


    /**
     * 搜索按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void searchMouseAction(MouseEvent mouseEvent) throws IOException {
        hintLabel.setText("");
        String word = inputTextField.getText();
        if(!isLegal(word)){
            hintLabel.setText("请输入英文单词！");
            return;
        }
        try {
            results = client.Translate.translate(word);
        } catch (SomeException e) {
            hintLabel.setText(e.getMessage());
            return;
        }

        //获取点赞数
        List<Integer> favoursTmp = ServerAPI.getFavoursNum(word);
        favours.replace("baidu", favoursTmp.get(0));
        favours.replace("youdao", favoursTmp.get(1));
        favours.replace("biying", favoursTmp.get(2));

        //检查该用户之前有没有对该单词点赞
        List<Boolean> userFavourTmp = ServerAPI.getUserFavour(Main.userName, word);
        userFavour.replace("baidu", userFavourTmp.get(0));
        userFavour.replace("youdao", userFavourTmp.get(1));
        userFavour.replace("biying", userFavourTmp.get(2));

        //根据点赞数排序
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(favours.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(userFavour.get(o1.getKey()) && !userFavour.get(o2.getKey()))
                return -1;
            else if(!userFavour.get(o1.getKey()) && userFavour.get(o2.getKey()))
                return 1;
            else
                return o2.getValue() - o1.getValue();
        });
        favours.clear();
        for(Map.Entry<String, Integer> entry: list){
            favours.put(entry.getKey(), entry.getValue());
        }

        showResult();
    }


    /**
     * 用正则表达式判断输入单词是否合法
     * @param word
     * @return
     */
    private boolean isLegal(String word) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+[\\s0-9a-zA-Z]*");
        Matcher matcher = pattern.matcher(word);
        return matcher.matches();
    }


    /**
     * 消息按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void messageMouseAction(MouseEvent mouseEvent) {
        Main.stageController.setStage("msgView", "queryView");
    }


    /**
     * 分享按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void shareMouseAction(MouseEvent mouseEvent) {
        WritableImage image = resultTextAreas[0].snapshot(new SnapshotParameters(), null);
        File file = new File("share.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Main.stageController.setStage("userView", "queryView");
    }


    /**
     * 点赞按钮点击事件
     * @param mouseEvent
     */
    @FXML
    public void favourMouseAction(MouseEvent mouseEvent) {
    }


    /**
     * 根据复选框和用户点赞数量显示结果
     */
    private void showResult(){
        //初始化
        for(Label toolLabel: toolLabels)
            toolLabel.setText("");
        for(TextArea resultTextArea: resultTextAreas)
            resultTextArea.setVisible(false);

        int index = 0;
        for(Map.Entry<String, Integer> entry: favours.entrySet()){
            String tool = entry.getKey();
            if(tool.equals("baidu")){
                if(!baiduCheckBox.isSelected())
                    continue;
                toolLabels[index].setText("百度");
                resultTextAreas[index].setVisible(true);
                resultTextAreas[index].setText(results.get("baidu"));
                index ++;
            }
            else if(tool.equals("youdao")){
                if(!youdaoCheckBox.isSelected())
                    continue;
                toolLabels[index].setText("有道");
                resultTextAreas[index].setVisible(true);
                resultTextAreas[index].setText(results.get("youdao"));
                index ++;
            }
            else if(tool.equals("biying")){
                if(!biyingCheckBox.isSelected())
                    continue;
                toolLabels[index].setText("必应");
                resultTextAreas[index].setVisible(true);
                resultTextAreas[index].setText(results.get("biying"));
                index ++;
            }
        }
    }
}
