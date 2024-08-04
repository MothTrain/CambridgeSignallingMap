module aradnezami.cambridgesignallingmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    
    
    opens aradnezami.cambridgesignallingmap to javafx.fxml;
    exports aradnezami.cambridgesignallingmap;
}