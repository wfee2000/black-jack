module htlleonding.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens htlleonding.frontend to javafx.fxml;
    exports htlleonding.frontend;
    opens htlleonding.frontend.Controller to javafx.fxml;
    exports htlleonding.frontend.Controller;
}