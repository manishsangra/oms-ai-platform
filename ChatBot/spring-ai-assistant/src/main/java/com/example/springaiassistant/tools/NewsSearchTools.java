
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class NewsSearchTools {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final Duration FALLBACK_WINDOW =
            Duration.ofHours(36);

    private static final int MAX_RESULTS = 5;

    private record NewsItem(
            String title,
            String description,
            String source,
            String pubDate,
            String link,
            Instant publishedAt
    ) {}

    private final HttpClient httpClient;

    public NewsSearchTools() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Tool(description = """
            Search the latest Indian news using Google News RSS.

            Use this tool when the user asks about latest news, breaking news,
            current affairs, Bollywood updates, movie releases, technology news,
            business news or sports updates.

            Provide a short search query describing the topic.
            The tool returns up to 5 recent news articles with headlines,
            short descriptions, publishers, publication dates and article links.

            Prioritise today's news. If today's articles are unavailable,
            return relevant articles from the last 36 hours.
            Never invent news stories.
            """)
    public String searchIndianNews(String query) {

        try {
            if (query == null || query.isBlank()) {
                query = "India latest news";
            }

            System.out.println("\n========== NEWS SEARCH STARTED ==========");

            System.out.println(
                    "News search query received from Gemini: " + query
            );

            LocalDate today = LocalDate.now(INDIA_ZONE);
            Instant now = Instant.now();

            System.out.println("Today's India date: " + today);

            String encodedQuery = URLEncoder.encode(
                    query,
                    StandardCharsets.UTF_8
            );

            String rssUrl = "https://news.google.com/rss/search?q="
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
                System.out.println(
                        "Google News HTTP error: " + response.statusCode()
                );

                return "News search is temporarily unavailable. HTTP status: "
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
                    "Google News RSS items received: " + items.getLength()
            );

            List<NewsItem> todayNews = new ArrayList<>();
            List<NewsItem> recentNews = new ArrayList<>();

            Set<String> seenTitles = new HashSet<>();

            int excludedGeneric = 0;
            int invalidDates = 0;
            int oldArticles = 0;
            int duplicateArticles = 0;

            for (int i = 0; i < items.getLength(); i++) {

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

                String normalizedTitle =
                        title.toLowerCase(Locale.ROOT).trim();

                // Exclude generic pages, not genuine news articles.
                if (isGenericNewsPage(normalizedTitle)) {
                    excludedGeneric++;
                    continue;
                }

                // Parse publication date.
                Instant publishedAt = parsePublicationDate(pubDate);

                if (publishedAt == null) {
                    invalidDates++;

                    System.out.println(
                            "Unable to parse RSS publication date: " + pubDate
                    );

                    continue;
                }

                LocalDate publicationDate = publishedAt
                        .atZone(INDIA_ZONE)
                        .toLocalDate();

                // Reject articles older than the fallback window.
                if (publishedAt.isBefore(now.minus(FALLBACK_WINDOW))) {
                    oldArticles++;
                    continue;
                }

                // Avoid duplicate headlines.
                String titleKey = normalizedTitle.replaceAll(
                        "[^a-z0-9]", ""
                );

                if (!seenTitles.add(titleKey)) {
                    duplicateArticles++;
                    continue;
                }

                NewsItem newsItem = new NewsItem(
                        title,
                        description,
                        source,
                        pubDate,
                        link,
                        publishedAt
                );

                // Prefer articles published on today's India date.
                if (publicationDate.equals(today)) {
                    todayNews.add(newsItem);
                }

                // Keep all articles within the recent 36-hour window
                // as a fallback if today's articles are unavailable.
                recentNews.add(newsItem);
            }

            // Newest articles first.
            Comparator<NewsItem> newestFirst =
                    Comparator.comparing(NewsItem::publishedAt).reversed();

            todayNews.sort(newestFirst);
            recentNews.sort(newestFirst);

            List<NewsItem> selectedNews;

            boolean usingFallback = false;

            if (!todayNews.isEmpty()) {
                selectedNews = todayNews;
            } else {
                selectedNews = recentNews;
                usingFallback = true;

                System.out.println(
                        "No articles found for today's date. "
                                + "Using recent 36-hour fallback."
                );
            }

            int limit = Math.min(
                    selectedNews.size(),
                    MAX_RESULTS
            );

            System.out.println("Today's articles: " + todayNews.size());

            System.out.println(
                    "Articles available in last 36 hours: "
                            + recentNews.size()
            );

            System.out.println(
                    "Generic pages excluded: " + excludedGeneric
            );

            System.out.println(
                    "Invalid publication dates: " + invalidDates
            );

            System.out.println(
                    "Older articles excluded: " + oldArticles
            );

            System.out.println(
                    "Duplicate articles excluded: " + duplicateArticles
            );

            System.out.println(
                    "Using 36-hour fallback: " + usingFallback
            );

            System.out.println(
                    "News results sent to Gemini: " + limit
            );

            if (limit == 0) {
                System.out.println(
                        "No usable news articles found."
                );

                System.out.println(
                        "========== NEWS SEARCH COMPLETED ==========\n"
                );

                return "No recent relevant Indian news articles were found. "
                        + "Do not invent news stories or create headlines.";
            }

            StringBuilder result = new StringBuilder();

            result.append("Recent Indian news articles");

            if (usingFallback) {
                result.append(" (published within the last 36 hours)");
            } else {
                result.append(" (published today: ").append(today).append(")");
            }

            result.append("\n\n");

            for (int i = 0; i < limit; i++) {

                NewsItem news = selectedNews.get(i);

                String formattedDate = DateTimeFormatter
                        .ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                        .withZone(INDIA_ZONE)
                        .format(news.publishedAt());

                System.out.println(
                        "Selected news: " + news.title()
                );

                result.append(i + 1)
                        .append(". ")
                        .append(news.title())
                        .append("\n");

                if (!news.source().isBlank()) {
                    result.append("Publisher: ")
                            .append(news.source())
                            .append("\n");
                }

                result.append("Published (IST): ")
                        .append(formattedDate)
                        .append("\n");

                if (!news.description().isBlank()) {
                    result.append("Description: ")
                            .append(news.description())
                            .append("\n");
                }

                result.append("Article link: ")
                        .append(news.link())
                        .append("\n\n");
            }

            System.out.println(
                    "========== NEWS SEARCH COMPLETED ==========\n"
            );

            return result.toString();

        } catch (Exception exception) {

            System.err.println("NEWS SEARCH ERROR:");

            exception.printStackTrace();

            return "Unable to retrieve current news at the moment. "
                    + "Reason: " + exception.getMessage();
        }
    }

    /**
     * Parses common RSS publication date formats.
     */
    private Instant parsePublicationDate(String pubDate) {

        if (pubDate == null || pubDate.isBlank()) {
            return null;
        }

        String value = pubDate.trim();

        // Standard Google News RSS date format.
        try {
            return ZonedDateTime.parse(
                    value,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant();

        } catch (Exception ignored) {
            // Try another supported format.
        }

        try {
            return OffsetDateTime.parse(value).toInstant();

        } catch (Exception ignored) {
            // Try ISO instant.
        }

        try {
            return Instant.parse(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Excludes generic pages and content that is not an individual article.
     */
    private boolean isGenericNewsPage(String title) {

        return containsAny(
                title,
                "school assembly",
                "daily news updates",
                "morning assembly",
                "live news streaming",
                "live tv",
                "news live",
                "ap top stories",
                "videos & photos",
                "video gallery",
                "photo gallery",
                "latest news, india news, breaking news"
        );
    }

    private boolean containsAny(
            String text,
            String... phrases
    ) {

        for (String phrase : phrases) {
            if (text.contains(phrase.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
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

        // Keep descriptions compact to reduce Gemini token consumption.
        if (cleaned.length() > 250) {
            cleaned = cleaned.substring(0, 250) + "...";
        }

        return cleaned;
    }
}
