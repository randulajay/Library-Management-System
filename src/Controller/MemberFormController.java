package Controller;

import Model.MemberTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DB;
import db.DBConnection;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MemberFormController {

    public JFXTextField mem_id;
    public JFXTextField mem_nme;
    public JFXTextField mem_addss;
    public JFXTextField mem_num;
    public TableView<MemberTM> mem_tbl;
    public ImageView img_bk;
    public AnchorPane root;
    public JFXButton btn_new;
    public JFXButton btn_add;

    //JDBC
    private Connection connection;
    private PreparedStatement selectall;
    private PreparedStatement newIdQuery;
    private PreparedStatement addToTable;
    private PreparedStatement updateQuarary;
    private PreparedStatement deleteQuarary;
    private PreparedStatement slectmemID;

    public void initialize() throws ClassNotFoundException {
        //disable id field
        mem_id.setDisable(true);
        Class.forName("com.mysql.jdbc.Driver");

        //set colman
        mem_tbl.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        mem_tbl.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        mem_tbl.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        mem_tbl.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("contact"));

        try {
            connection = DBConnection.getInstance().getConnection();
            ObservableList<MemberTM> members = mem_tbl.getItems();
            selectall = connection.prepareStatement("SELECT * from memberdetail");
            slectmemID = connection.prepareStatement("select * from memberdetail where id=?");
            newIdQuery = connection.prepareStatement("SELECT id FROM memberdetail");
            addToTable = connection.prepareStatement("INSERT INTO memberdetail values(?,?,?,?)");
            updateQuarary = connection.prepareStatement("UPDATE memberdetail SET name=? , address=? , contact=? where id=?");
            ResultSet rst = selectall.executeQuery();
            while (rst.next()) {
                System.out.println("load");
                members.add(new MemberTM(
                        rst.getString(1),
                        rst.getString(2),
                        rst.getString(3),
                        rst.getString(4)
                ));
            }
            mem_tbl.setItems(members);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mem_tbl.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MemberTM>() {
            @Override
            public void changed(ObservableValue<? extends MemberTM> observable, MemberTM oldValue, MemberTM newValue) {
                MemberTM selectedItem = mem_tbl.getSelectionModel().getSelectedItem();
                try {
                    connection = null;
                    try {
                        slectmemID.setString(1, selectedItem.getId());
                        ResultSet rst = slectmemID.executeQuery();
                        if (rst.next()) {
                            mem_id.setText(rst.getString(1));
                            mem_nme.setText(rst.getString(2));
                            mem_addss.setText(rst.getString(3));
                            mem_num.setText(rst.getString(4));
                            mem_id.setDisable(true);
                            btn_add.setText("Update");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (NullPointerException n) {
                    return;
                }
            }
        });
    }

    //button new action
    public void btn_new(ActionEvent actionEvent) throws SQLException {
        //txt_valid.setText("");
        mem_nme.clear();
        mem_addss.clear();
        mem_num.clear();
        btn_add.setText("Add");
        mem_id.setDisable(false);

        ResultSet rst = newIdQuery.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()) {
            ids = rst.getString(1);

            int id = Integer.parseInt(ids.replace("M", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "M00" + maxId;
        } else if (maxId < 100) {
            id = "M0" + maxId;
        } else {
            id = "M" + maxId;
        }
        mem_id.setText(id);
    }

    //button add action
    public void btn_add(ActionEvent actionEvent) throws SQLException {
        ObservableList<MemberTM> members = FXCollections.observableList(DB.members);
        //is empty
        if (mem_id.getText().isEmpty() ||
                mem_nme.getText().isEmpty() ||
                mem_addss.getText().isEmpty() ||
                mem_num.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill your details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }
        //reg-ex
        if (!(mem_nme.getText().matches("^\\b([A-Za-z.]+\\s?)+$") && mem_addss.getText().matches("^\\b[A-Za-z0-9/,\\s]+.$") && mem_num.getText().matches("\\d{10}"))) {
            new Alert(Alert.AlertType.ERROR, "Entered detail Invalid").show();
            return;
        }

        //save & update
        if (btn_add.getText().equals("Add")) {
            addToTable.setString(1, mem_id.getText());
            addToTable.setString(2, mem_nme.getText());
            addToTable.setString(3, mem_addss.getText());
            addToTable.setString(4, mem_num.getText());
            int affectedRows = addToTable.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Data load successful");
            } else {
                System.out.println("Something went wrong");
            }
        } else {
            if (btn_add.getText().equals("Update")) {
                for (int i = 0; i < members.size(); i++) {
                    if (mem_id.getText().equals(members.get(i).getId())) {
                        try {
                            updateQuarary.setString(1, mem_nme.getText());
                            updateQuarary.setString(2, mem_addss.getText());
                            updateQuarary.setString(3, mem_num.getText());
                            updateQuarary.setString(4, mem_id.getText());
                            int affected = updateQuarary.executeUpdate();

                            if (affected > 0) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                        "Record updated!!",
                                        ButtonType.OK);
                                Optional<ButtonType> buttonType = alert.showAndWait();
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR,
                                        "Update error!",
                                        ButtonType.OK);
                                Optional<ButtonType> buttonType = alert.showAndWait();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mem_tbl.setItems(members);
        }
        try {
            mem_tbl.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //btn add delete
    public void btn_dtl(ActionEvent actionEvent) throws SQLException {
        MemberTM selectedItem = mem_tbl.getSelectionModel().getSelectedItem();
        if (mem_tbl.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select a member.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        } else {
            deleteQuarary = connection.prepareStatement("DELETE from memberdetail where id=?");
            deleteQuarary.setString(1, selectedItem.getId());

            int affected = deleteQuarary.executeUpdate();
            if (affected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Record deleted!!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
            }
        }
        try {
            mem_tbl.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void playMouseEnterAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();

            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.YELLOW);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(20);
            icon.setEffect(glow);
        }
    }

    public void img_back(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/View/HomeFormView.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.root.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }
}
