package com.example.demo.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public PdfService() {
        List<Extension> extensions = List.of(
                TablesExtension.create()
        );

        this.markdownParser = Parser.builder()
                .extensions(extensions)
                .build();

        this.htmlRenderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    public byte[] generatePdf(String title, String content) {

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Node document = markdownParser.parse(
                    content == null ? "" : content
            );

            String body = htmlRenderer.render(document);
            String html = buildHtmlDocument(title, body);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error generando PDF",
                    e
            );
        }
    }

    private String buildHtmlDocument(String title, String body) {
        String escapedTitle = escapeHtml(
                title == null || title.isBlank()
                        ? "Informe"
                        : title
        );

        return """
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <title>%s</title>
                    <style>
                        @page {
                            size: A4;
                            margin: 24mm 20mm;
                        }

                        body {
                            color: #1f2933;
                            font-family: Arial, Helvetica, sans-serif;
                            font-size: 11pt;
                            line-height: 1.55;
                        }

                        h1 {
                            color: #111827;
                            font-size: 22pt;
                            margin: 0 0 18px;
                        }

                        h2 {
                            border-bottom: 1px solid #d9dee7;
                            color: #1f2933;
                            font-size: 16pt;
                            margin: 24px 0 10px;
                            padding-bottom: 5px;
                        }

                        h3 {
                            color: #334155;
                            font-size: 13pt;
                            margin: 18px 0 8px;
                        }

                        p {
                            margin: 0 0 10px;
                        }

                        ul, ol {
                            margin: 0 0 12px 22px;
                            padding: 0;
                        }

                        li {
                            margin-bottom: 4px;
                        }

                        table {
                            border-collapse: collapse;
                            margin: 12px 0 16px;
                            width: 100%%;
                        }

                        th, td {
                            border: 1px solid #cfd6e0;
                            padding: 6px 8px;
                            text-align: left;
                            vertical-align: top;
                        }

                        th {
                            background: #eef2f7;
                            font-weight: 700;
                        }

                        blockquote {
                            border-left: 4px solid #cfd6e0;
                            color: #475569;
                            margin: 12px 0;
                            padding: 4px 0 4px 12px;
                        }

                        code {
                            background: #f3f4f6;
                            border-radius: 3px;
                            font-family: "Courier New", monospace;
                            padding: 1px 3px;
                        }

                        pre {
                            background: #f3f4f6;
                            border-radius: 4px;
                            font-family: "Courier New", monospace;
                            padding: 10px;
                            white-space: pre-wrap;
                        }

                        hr {
                            border: 0;
                            border-top: 1px solid #d9dee7;
                            margin: 18px 0;
                        }
                    </style>
                </head>
                <body>
                    %s
                </body>
                </html>
                """.formatted(escapedTitle, body).stripLeading();
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
