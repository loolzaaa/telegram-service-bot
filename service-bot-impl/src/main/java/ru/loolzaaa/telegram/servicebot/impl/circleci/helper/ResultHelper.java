package ru.loolzaaa.telegram.servicebot.impl.circleci.helper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class ResultHelper {

    private static final int WIDTH = 560;
    private static final int HEIGHT = 560;

    public static byte[] getResult(Result resultType, ResultData resultData) {
        final String html = String.format(
                HTML_TEMPLATE,
                resultType.getIconUrl(),
                resultType.getText(),
                resultData.getCommitAuthor(),
                resultData.getProjectName(),
                resultData.getWorkflowName(),
                resultData.getBranchName(),
                resultData.getCommitName(),
                resultData.getCommitHash(),
                resultData.getJobName(),
                resultType.getIconUrl(),
                resultType.getShortText());

        final Dimension dimension = new Dimension(WIDTH, HEIGHT);
        final SyncHTMLEditorKit editorKit = new SyncHTMLEditorKit();
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setSize(dimension);
        editorPane.setEditable(false);
        editorPane.setEditorKit(editorKit);

        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(String.format(".main-container {font-size: 14px; width: %dpx; }", WIDTH));
        styleSheet.addRule(".main-table {width: 100%; }");
        styleSheet.addRule(".info-table {padding: 20px; width: 100%; }");
        styleSheet.addRule(String.format(".workflow-header {color: #ffffff; font-weight: bold; letter-spacing: 0.5px; padding: 20px; text-align: center; background-color: #%s; }", resultType.getColor()));
        styleSheet.addRule(".job-name-column {width: 43%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }");
        styleSheet.addRule(String.format(".job-status {color: #%s; font-weight: bold; vertical-align: middle;}", resultType.getColor()));
        styleSheet.addRule(".mark-text {color: #337ab7; }");

        Document doc = editorKit.createDefaultDocument();
        editorPane.setDocument(doc);
        editorPane.setText(html);

        Dimension prefSize = editorPane.getPreferredSize();
        BufferedImage img = new BufferedImage(prefSize.width, prefSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        editorPane.setSize(prefSize);
        editorPane.paint(graphics);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final String formatName = "png";
        try {
            ImageIO.write(img, formatName, byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Exception while saving image", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public enum Result {
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

    public static class ResultData {
        private final String commitAuthor;
        private final String projectName;
        private final String workflowName;
        private final String branchName;
        private final String commitName;
        private final String commitHash;
        private final String jobName;

        public ResultData(String commitAuthor, String projectName, String workflowName, String branchName,
                          String commitName, String commitHash, String jobName) {
            this.commitAuthor = commitAuthor;
            this.projectName = projectName;
            this.workflowName = workflowName;
            this.branchName = branchName;
            this.commitName = commitName;
            this.commitHash = commitHash;
            this.jobName = jobName;
        }

        public String getCommitAuthor() {
            return commitAuthor;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getWorkflowName() {
            return workflowName;
        }

        public String getBranchName() {
            return branchName;
        }

        public String getCommitName() {
            return commitName;
        }

        public String getCommitHash() {
            return commitHash;
        }

        public String getJobName() {
            return jobName;
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
            "          <center><img src=\"%s\" width=\"80\" height=\"80\"/></center>" +
            "        </td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td class=\"workflow-header\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td>" +
            "          <table class=\"info-table\" style=\"border-bottom: 1px solid black;\">" +
            "            <tbody>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Author</td>" +
            "                <td style=\"font-family: Monospace;\">%s</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Project</td>" +
            "                <td style=\"font-family: Monospace;\" class=\"mark-text\">%s</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Workflow</td>" +
            "                <td style=\"font-family: Monospace;\" class=\"mark-text\">%s</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Branch</td>" +
            "                <td style=\"font-family: Monospace;\" class=\"mark-text\">%s</td>" +
            "              </tr>" +
            "              <tr>" +
            "                <td style=\"font-weight: bold; width: 18%%;\">Commit</td>" +
            "                <td style=\"font-family: Monospace;\">" +
            "                  <div>%s <span class=\"mark-text\">%s</span></div>" +
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
            "                <td class=\"job-name-column mark-text\" style=\"font-family: Monospace;\">%s</td>" +
            "                <td>" +
            "                  <img src=\"%s\" width=\"20\" height=\"20\" style=\"vertical-align: middle;\"/>" +
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

    static class SyncHTMLEditorKit extends HTMLEditorKit {

        public Document createDefaultDocument() {
            HTMLDocument doc = (HTMLDocument) super.createDefaultDocument();
            doc.setAsynchronousLoadPriority(-1);
            return doc;
        }

        public ViewFactory getViewFactory() {
            return new HTMLFactory() {
                public View create(Element elem) {
                    View view = super.create(elem);
                    if (view instanceof ImageView) {
                        ((ImageView) view).setLoadsSynchronously(true);
                    }
                    return view;
                }
            };
        }
    }
}
