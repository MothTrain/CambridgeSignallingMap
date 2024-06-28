module aradnezami.cambridgesignallingmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    
    
    opens aradnezami.cambridgesignallingmap to javafx.fxml;
    exports aradnezami.cambridgesignallingmap;
}