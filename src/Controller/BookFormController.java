package Controller;

import Model.BookTM;
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

public class BookFormController {
    public JFXTextField txt_bk_id;
    public JFXTextField txt_bk_title;
    public JFXTextField txt_bk_auth;
    public JFXTextField txt_bk_st;
    public TableView<BookTM> tbl_bk;
    public AnchorPane bk_root;
    public JFXButton btn_add;
    private Connection connection;

    //JDBC
    private PreparedStatement selectall;
    private PreparedStatement selectID;
    private PreparedStatement newIdQuery;
    private PreparedStatement addToTable;
    private PreparedStatement updateQuarary;
    private PreparedStatement deleteQuarary;

    public void initialize() throws ClassNotFoundException {
        //disable id field
        txt_bk_id.setDisable(true);

        //load table
        tbl_bk.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tbl_bk.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("title"));
        tbl_bk.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("author"));
        tbl_bk.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("status"));

        Class.forName("com.mysql.jdbc.Driver");

        try {
            connection = DBConnection.getInstance().getConnection();
            selectall = connection.prepareStatement("SELECT * from bookdetail");
            updateQuarary = connection.prepareStatement("UPDATE bookdetail SET title=? , author=? , status=? where id=?");
            selectID = connection.prepareStatement("select * from bookdetail where id=?");
            addToTable = connection.prepareStatement("INSERT INTO bookdetail values(?,?,?,?)");
            newIdQuery = connection.prepareStatement("SELECT id from bookdetail");
            deleteQuarary = connection.prepareStatement("DELETE from bookdetail where id=?");
            ObservableList<BookTM> members = tbl_bk.getItems();
            ResultSet rst = selectall.executeQuery();
            while (rst.next()) {
                System.out.println("load");
                members.add(new BookTM(
                        rst.getString(1),
                        rst.getString(2),
                        rst.getString(3),
                        rst.getString(4)
                ));
            }
            tbl_bk.setItems(members);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tbl_bk.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BookTM>() {
            @Override
            public void changed(ObservableValue<? extends BookTM> observable, BookTM oldValue, BookTM newValue) {
                BookTM selectedItem = tbl_bk.getSelectionModel().getSelectedItem();
                try {
                    connection = null;
                    try {
                        selectID.setString(1, selectedItem.getId());
                        ResultSet rst = selectID.executeQuery();

                        if (rst.next()) {
                            txt_bk_id.setText(rst.getString(1));
                            txt_bk_title.setText(rst.getString(2));
                            txt_bk_auth.setText(rst.getString(3));
                            txt_bk_st.setText(rst.getString(4));
                            txt_bk_id.setDisable(true);
                            btn_add.setText("Update");
                        }
                        btn_add.setText("Update");
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
        btn_add.setText("Add");
        txt_bk_st.setText("Available");
        txt_bk_st.setDisable(true);
        txt_bk_id.setDisable(false);
        txt_bk_auth.clear();
        txt_bk_title.clear();
        txt_bk_title.requestFocus();

        ResultSet rst = newIdQuery.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()) {
            ids = rst.getString(1);

            int id = Integer.parseInt(ids.replace("B", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "B00" + maxId;
        } else if (maxId < 100) {
            id = "B0" + maxId;
        } else {
            id = "B" + maxId;
        }
        txt_bk_id.setText(id);
    }

    //button add action
    public void btn_Add(ActionEvent actionEvent) throws SQLException {
        ObservableList<BookTM> books = FXCollections.observableList(DB.books);

        if (txt_bk_id.getText().isEmpty() || txt_bk_title.getText().isEmpty() || txt_bk_auth.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill your details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }

        //reg ex
        if (!(txt_bk_title.getText().matches("^\\b([A-Za-z.]+\\s?)+$") && txt_bk_auth.getText().matches("^\\b([A-Za-z.]+\\s?)+$"))) {
            new Alert(Alert.AlertType.ERROR, "Enter Valid Name").show();
            return;
        }
        if (btn_add.getText().equals("Add")) {
            addToTable.setString(1, txt_bk_id.getText());
            addToTable.setString(2, txt_bk_title.getText());
            addToTable.setString(3, txt_bk_auth.getText());
            addToTable.setString(4, txt_bk_st.getText());

            int affectedRows = addToTable.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Data insertion successfull");
            } else {
                System.out.println("ERROR");
            }
        } else {
            if (btn_add.getText().equals("Update")) {
                for (int i = 0; i < books.size(); i++) {
                    if (txt_bk_id.getText().equals(books.get(i).getId())) {
                        updateQuarary.setString(1, txt_bk_title.getText());
                        updateQuarary.setString(2, txt_bk_auth.getText());
                        updateQuarary.setString(3, txt_bk_st.getText());
                        updateQuarary.setString(4, txt_bk_id.getText());
                        int affected = updateQuarary.executeUpdate();

                        if (affected > 0) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "Record updated!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Update error!",
                                    ButtonType.OK);
                            Optional<ButtonType> buttonType = alert.showAndWait();
                        }
                    }
                }
                tbl_bk.setItems(books);
            }
        }
        try {
            tbl_bk.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //button delete action
    public void btn_dlt(ActionEvent actionEvent) throws SQLException {
        BookTM selectedItem = tbl_bk.getSelectionModel().getSelectedItem();
        if (tbl_bk.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select a member.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        } else {
            deleteQuarary.setString(1, selectedItem.getId());
            int affected = deleteQuarary.executeUpdate();

            if (affected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Record deleted!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
            }
        }
        try {
            tbl_bk.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void img_back(MouseEvent event) throws IOException {

        URL resource = this.getClass().getResource("/View/HomeFormView.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.bk_root.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
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
}
