
package com.example.springaiassistant.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class CurrentInfoTools {

    private static final int MAX_RESULTS = 8;

    private final HttpClient httpClient;

    public CurrentInfoTools() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Tool(description = """
            Search current and upcoming events using Google News RSS.

            Use this tool when the user asks about:
            - Future elections and government announcements.
            - Upcoming movies and release dates.
            - Upcoming sports matches and tournaments.
            - Upcoming examinations and public events.
            - Future schedules, announcements and planned events.

            Do not use this tool for ordinary historical or general knowledge
            questions.

            Search using a specific query containing the subject and
            relevant future-event keywords.

            Return relevant articles with their headlines, publishers,
            publication dates, descriptions and source links.

            Distinguish confirmed announcements from tentative dates,
            speculation and rumours. An article's publication date is
            not the event date.
            """)
    public String searchCurrentInformation(String query) {

        try {

            if (query == null || query.isBlank()) {
                return "Please provide a topic to search.";
            }

            System.out.println(
                    "\n========== CURRENT INFORMATION SEARCH STARTED =========="
            );

            System.out.println(
                    "Search query: " + query
            );

            String encodedQuery = URLEncoder.encode(
                    query,
                    StandardCharsets.UTF_8
            );

            String rssUrl =
                    "https://news.google.com/rss/search?q="
                            + encodedQuery
                            + "&hl=en-IN&gl=IN&ceid=IN:en";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rssUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "ManishAI/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

            if (response.statusCode() != 200) {
                return "Current information search is unavailable. HTTP status: "
                        + response.statusCode();
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(response.body()))
            );

            NodeList items = document.getElementsByTagName("item");

            System.out.println(
                    "RSS articles received: " + items.getLength()
            );

            List<String> articles = new ArrayList<>();

            for (int i = 0;
                 i < items.getLength() && articles.size() < MAX_RESULTS;
                 i++) {

                Element item = (Element) items.item(i);

                String title = getTagValue(item, "title");
                String description = cleanDescription(
                        getTagValue(item, "description")
                );

                String link = getTagValue(item, "link");
                String pubDate = getTagValue(item, "pubDate");
                String source = getSource(item);

                if (title.isBlank() || link.isBlank()) {
                    continue;
                }

                StringBuilder article = new StringBuilder();

                article.append("Headline: ")
                        .append(title)
                        .append("\n");

                if (!source.isBlank()) {
                    article.append("Publisher: ")
                            .append(source)
                            .append("\n");
                }

                if (!pubDate.isBlank()) {
                    article.append("Published: ")
                            .append(pubDate)
                            .append("\n");
                }

                if (!description.isBlank()) {
                    article.append("Description: ")
                            .append(description)
                            .append("\n");
                }

                article.append("Source link: ")
                        .append(link);

                articles.add(article.toString());
            }

            System.out.println(
                    "Articles returned: " + articles.size()
            );

            System.out.println(
                    "========== CURRENT INFORMATION SEARCH COMPLETED ==========\n"
            );

            if (articles.isEmpty()) {
                return "No relevant articles were found. "
                        + "Do not invent information or event dates.";
            }

            return "Current information search results for: "
                    + query
                    + "\n\n"
                    + String.join("\n\n", articles);

        } catch (Exception exception) {

            System.err.println(
                    "CURRENT INFORMATION SEARCH ERROR:"
            );

            exception.printStackTrace();

            return "Unable to retrieve current information. "
                    + exception.getMessage();
        }
    }

    private String getTagValue(
            Element element,
            String tagName
    ) {

        NodeList nodes = element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return "";
        }

        return nodes.item(0).getTextContent().trim();
    }

    private String getSource(Element item) {

        NodeList sources = item.getElementsByTagName("source");

        if (sources.getLength() == 0) {
            return "";
        }

        return sources.item(0).getTextContent().trim();
    }

    private String cleanDescription(String description) {

        if (description == null || description.isBlank()) {
            return "";
        }

        String cleaned = description
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.length() > 300) {
            cleaned = cleaned.substring(0, 300) + "...";
        }

        return cleaned;
    }
}
