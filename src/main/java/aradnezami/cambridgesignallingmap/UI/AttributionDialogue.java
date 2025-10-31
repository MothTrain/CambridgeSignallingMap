package aradnezami.cambridgesignallingmap.UI;

import javax.swing.*;

public class AttributionDialogue {
    private static final String message = """
            PythonCommunications package:
                    Derived from the "td-trust-example-python3" Github repository developed
                    by EvelynSubarrow as part of the "openraildata" Github group. The repo has
                    been modified(/mangled) extensively to fit the use of this project and
                    was licenced under an "MIT No Attribution License".
                    REPOSITORY: https://github.com/openraildata/td-trust-example-python3
                    DEVELOPER: https://github.com/EvelynSubarrow
            
            Fonts:
                    This project utilises 2 main fonts for it's diagram. The font files can
                    be found in the resources folder. The "Pixeloid Mono" font (used for
                    displaying headcodes) and the "Home Video" font (used for most other
                    digital looking fonts) were both developed by GGBotNet under the
                    OpenFontLicense.
                    PIXELOID MONO FONT: https://ggbot.itch.io/pixeloid-font
                    HOME VIDEO FONT: https://ggbot.itch.io/home-video-font
                    CREATOR: https://ggbot.itch.io/
            
            And, of course, many thanks to the entire UK Rail Open Data community, especially
            the contributors to the Open Rail Data Wiki, who, without their knowledge, I
            would not have been able to make this project.
            
            Thanks again :)
            - Arad Nezami (MothTrain)
            """;

    public static void display(JComponent parent) {
        JOptionPane.showMessageDialog(parent, message, "Attribution", JOptionPane.INFORMATION_MESSAGE);
    }
}
