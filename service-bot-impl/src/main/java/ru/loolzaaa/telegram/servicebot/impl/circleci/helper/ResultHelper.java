package ru.loolzaaa.telegram.servicebot.impl.circleci.helper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class ResultHelper {

    public static String getResult(Result resultType) {
        final String html = String.format(
                HTML_TEMPLATE,
                resultType.getIconUrl(),
                resultType.getText(),
                resultType.getIconUrl(),
                resultType.getShortText());

        final Dimension dimension = new Dimension(800, 560);
        final HTMLEditorKit editorKit = new HTMLEditorKit();
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setSize(dimension);
        editorPane.setEditable(false);
        editorPane.setEditorKit(editorKit);
        editorPane.setMargin(new Insets(0,0,0,0));

        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule("html body {margin: 0; padding: 0; }");
        styleSheet.addRule(".main-container {font-size: 14px; }");
        styleSheet.addRule(".main-table {width: 480px; }");
        styleSheet.addRule(".top-logo {display: block; width: 80px; height: 80px; margin-left: auto; margin-right: auto; }");
        styleSheet.addRule(".bottom-logo {width: 20px; height: 20px; vertical-align: middle; }");
        styleSheet.addRule(".job-name-column {width: 43%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }");
        styleSheet.addRule(String.format(".job-status {color: #%s; font-weight: bold; vertical-align: middle; }", resultType.getColor()));
        styleSheet.addRule(String.format(".workflow-header {border-radius: 3px 3px 0 0; color: #ffffff; font-weight: bold; letter-spacing: 0.5px; padding: 20px; text-align: center; background-color: #%s; }", resultType.getColor()));
        styleSheet.addRule(".info-table {padding: 20px; width: 100%; }");
        styleSheet.addRule(".mark-text {color: #337ab7; }");

        Document doc = editorKit.createDefaultDocument();
        editorPane.setDocument(doc);
        editorPane.setText(html);

        Dimension prefSize = editorPane.getPreferredSize();
        BufferedImage img = new BufferedImage(prefSize.width, prefSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = img.getGraphics();
        editorPane.setSize(prefSize);
        editorPane.paint(graphics);
        try {
            final String formatName = "png";
            ImageIO.write(img, formatName, new File("img.png"));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Exception while saving '%s' image", "img"), e);
        }

        return html;
    }

    enum Result {
        SUCCESS(
                "42c98b",
                "Success",
                "Your workflow completed successfully.",
                "https://dmmj3mmt94rvw.cloudfront.net/img/email/passed/icon@3x.png"),
        FAILED(
                "ed5c5c",
                "Failed",
                "Uh-oh, this workflow did not succeed.",
                "https://dmmj3mmt94rvw.cloudfront.net/img/email/failed/icon@3x.png");

        private final String color;
        private final String shortText;
        private final String text;
        private final String iconUrl;

        Result(String color, String shortText, String text, String iconUrl) {
            this.color = color;
            this.shortText = shortText;
            this.text = text;
            this.iconUrl = iconUrl;
        }

        public String getColor() {
            return color;
        }

        public String getShortText() {
            return shortText;
        }

        public String getText() {
            return text;
        }

        public String getIconUrl() {
            return iconUrl;
        }
    }

    public static final String HTML_TEMPLATE = "<!doctype html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "  <meta charset=\"utf-8\">" +
            "  <title>CircleCI Result</title>" +
            "  <meta name=\"author\" content=\"loolzaaa\">" +
            "</head>" +
            "<body>" +
            "<div class=\"main-container\">" +
            "  <table class=\"main-table\">" +
            "    <tbody>" +
            "      <tr>" +
            "        <td>" +
            "          <img class=\"top-logo\" src=\"%s\">" +
            "        </td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td class=\"workflow-header\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td>" +
            "          <table class=\"info-table\">" +
            "            <tbody>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Author</td>" +
            "                <td style=\"font-family: 'Courier';\">Anastasia66627</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Project</td>" +
            "                <td style=\"font-family: 'Courier';\" class=\"mark-text\">PNPPK/new-product-tracker-back</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Workflow</td>" +
            "                <td style=\"font-family: 'Courier';\" class=\"mark-text\">main</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Branch</td>" +
            "                <td style=\"font-family: 'Courier';\" class=\"mark-text\">master</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Commit</td>" +
            "                <td style=\"font-family: 'Courier';\">" +
            "                  <div>Add OperationServiceImplTest <span class=\"mark-text\">5592240</span></div>" +
            "                </td>" +
            "              </tr>" +
            "            </tbody>" +
            "          </table>" +
            "        </td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td>" +
            "          <table class=\"info-table\">" +
            "            <tbody>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Jobs</td>" +
            "                <td style=\"font-family: 'Courier';\" class=\"job-name-column mark-text\">build-and-test</td>" +
            "                <td>" +
            "                  <img class=\"bottom-logo\" src=\"%s\">" +
            "                  <span class=\"job-status\">%s</span>" +
            "                </td>" +
            "              </tr>" +
            "            </tbody>" +
            "          </table>" +
            "        </td>" +
            "      </tr>" +
            "    </tbody>" +
            "  </table>" +
            "</div>" +
            "</body>";
}
