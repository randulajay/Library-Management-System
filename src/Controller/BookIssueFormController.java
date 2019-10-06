package Controller;

import Model.BookIssueTM;
import Model.BookTM;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
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

public class BookIssueFormController {
    public JFXTextField txt_issid;
    public JFXDatePicker txt_isu_date;
    public JFXTextField txt_name;
    public JFXTextField txt_title;
    public JFXComboBox mem_is_id;
    public JFXComboBox book_id;
    public TableView<BookIssueTM> bk_ssue_tbl;
    public AnchorPane bk_iss;
    private Connection connection;

    //JDBC
    private PreparedStatement selectALl;
    private PreparedStatement selectmemID;
    private PreparedStatement selectbkdtl;
    private PreparedStatement table;
    private PreparedStatement delete;

    public void initialize() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");

        bk_ssue_tbl.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("issueId"));
        bk_ssue_tbl.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        bk_ssue_tbl.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("memberId"));
        bk_ssue_tbl.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("bookId"));


        try {
            connection = DBConnection.getInstance().getConnection();
            ObservableList<BookIssueTM> issue = bk_ssue_tbl.getItems();

            selectALl = connection.prepareStatement("SELECT * FROM issuetb");
            selectmemID = connection.prepareStatement("select name from memberdetail where id=?");
            selectbkdtl = connection.prepareStatement("select title,status from bookdetail where id=?");
            table = connection.prepareStatement("INSERT INTO issuetb values(?,?,?,?)");
            delete = connection.prepareStatement("DELETE FROM issuetb WHERE issueId=?");
            ResultSet rst = selectALl.executeQuery();

            while (rst.next()) {
                System.out.println("load");
                issue.add(new BookIssueTM(rst.getString(1),
                        rst.getString(2),
                        rst.getString(3),
                        rst.getString(4)));
            }

            bk_ssue_tbl.setItems(issue);
            mem_is_id.getItems().clear();
            ObservableList cmbmembers = mem_is_id.getItems();
            String sql2 = "select id from memberdetail";
            PreparedStatement pstm1 = connection.prepareStatement(sql2);
            ResultSet rst1 = pstm1.executeQuery();

            while (rst1.next()) {
                cmbmembers.add(rst1.getString(1));
            }

            book_id.getItems().clear();
            ObservableList cmbbooks = book_id.getItems();
            String sql3 = "select id from bookdetail";
            PreparedStatement pstm2 = connection.prepareStatement(sql3);
            ResultSet rst2 = pstm2.executeQuery();
            while (rst2.next()) {
                cmbbooks.add(rst2.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mem_is_id.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {

                if (mem_is_id.getSelectionModel().getSelectedItem() != null) {
                    Object selectedItem = mem_is_id.getSelectionModel().getSelectedItem();
                    if (selectedItem.equals(null) || mem_is_id.getSelectionModel().isEmpty()) {
                        return;
                    }
                    try {
                        selectmemID.setString(1, selectedItem.toString());
                        ResultSet rst = selectmemID.executeQuery();

                        if (rst.next()) {
                            txt_name.setText(rst.getString(1));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        book_id.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (book_id.getSelectionModel().getSelectedItem() != null) {
                    Object selectedItem = book_id.getSelectionModel().getSelectedItem();

                    try {
                        txt_title.clear();
                        selectbkdtl.setString(1, selectedItem.toString());
                        ResultSet rst = selectbkdtl.executeQuery();

                        if (rst.next()) {
                            if (rst.getString(2).equals("Available")) {
                                txt_title.setText(rst.getString(1));
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR,
                                        "This book isn't available!",
                                        ButtonType.OK);
                                Optional<ButtonType> buttonType = alert.showAndWait();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //button new action
    public void new_action(ActionEvent actionEvent) throws SQLException {
        txt_title.clear();
        txt_name.clear();
        mem_is_id.getSelectionModel().clearSelection();
        book_id.getSelectionModel().clearSelection();
        txt_isu_date.setPromptText("Issue Date");

        String sql = "Select issueId from issuetb";
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()) {
            ids = rst.getString(1);

            int id = Integer.parseInt(ids.replace("I", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "I00" + maxId;
        } else if (maxId < 100) {
            id = "I0" + maxId;
        } else {
            id = "I" + maxId;
        }
        txt_issid.setText(id);
    }

    //button add action
    public void add_Action(ActionEvent actionEvent) throws SQLException {

        ObservableList<BookIssueTM> issued = FXCollections.observableList(DB.issued);
        ObservableList<BookTM> books = FXCollections.observableList(DB.books);

        if (txt_issid.getText().isEmpty() ||
                book_id.getSelectionModel().getSelectedItem().equals(null) ||
                mem_is_id.getSelectionModel().getSelectedItem().equals(null)
                || txt_isu_date.getValue().toString().equals(null)) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill your details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        } else {
            String memberId = (String) mem_is_id.getSelectionModel().getSelectedItem();
            String bookId = (String) book_id.getSelectionModel().getSelectedItem();
            issued.add(new BookIssueTM(txt_issid.getText(), txt_isu_date.getValue().toString(), memberId, bookId));

            try {
                table.setString(1, txt_issid.getText());
                table.setString(2, txt_isu_date.getValue().toString());
                table.setString(3, (String) mem_is_id.getSelectionModel().getSelectedItem());
                table.setString(4, (String) book_id.getSelectionModel().getSelectedItem());
                int affectedRows = table.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Data insertion successfull");
                    String sql2 = "Update bookdetail SET status=? where id=?";
                    PreparedStatement pstm2 = connection.prepareStatement(sql2);
                    String id = (String) book_id.getSelectionModel().getSelectedItem();
                    pstm2.setString(1, "Unavailable");
                    pstm2.setString(2, id);
                    int affected = pstm2.executeUpdate();

                    if (affected > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                "Status updated.",
                                ButtonType.OK);
                        Optional<ButtonType> buttonType = alert.showAndWait();
                    } else {
                        System.out.println("ERROR");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            bk_ssue_tbl.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //button delete action
    public void delete_Action(ActionEvent actionEvent) throws SQLException {
        //BookIssueTM selectedItem = (BookIssueTM) FXCollections.observableList(DB.issued);
        BookIssueTM selectedItem = bk_ssue_tbl.getSelectionModel().getSelectedItem();
        if (bk_ssue_tbl.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select a raw.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        } else {
            try {
                delete.setString(1, selectedItem.getIssueId());
                delete.executeUpdate();

                String sql2 = "Update bookdetail SET status=? where id=?";
                PreparedStatement pstm2 = connection.prepareStatement(sql2);
                String id = (String) book_id.getSelectionModel().getSelectedItem();
                pstm2.setString(1, "Available");
                pstm2.setString(2, id);
                pstm2.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Record deleted!",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        try {
            bk_ssue_tbl.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void back_click(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/View/HomeFormView.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.bk_iss.getScene().getWindow();
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
