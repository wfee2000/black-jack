module htlleonding.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens at.htlleonding.frontend to javafx.fxml;
    exports at.htlleonding.frontend;
    opens at.htlleonding.frontend.Controller to javafx.fxml;
    exports at.htlleonding.frontend.Controller;
    exports at.htlleonding.frontend.model;
}