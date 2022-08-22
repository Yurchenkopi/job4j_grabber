package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.stream.Collectors;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int PAGE_NUM = 5;

    private String retrieveDescription(String link) throws IOException {
        Connection cn = Jsoup.connect(link);
        Document doc = cn.get();
        return doc.select(".collapsible-description__content")
                .stream()
                .map(el -> new StringBuilder().append(el.select(".style-ugc").text()))
                .collect(Collectors.joining());
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse hcp = new HabrCareerParse();
        for (int page = 1; page <= PAGE_NUM; page++) {
            Connection connection = Jsoup.connect(PAGE_LINK + page);
            Document document = connection.get();
            String rowSeparator = "-".repeat(30).concat(System.lineSeparator());
            System.out.printf("%s%s%n", rowSeparator, "Чтение данных страницы: " + page);
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element date = row.select(".vacancy-card__date").first().child(0);
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                try {
                    System.out.printf("%s%s %s %s%n%s%n",
                            rowSeparator,
                            vacancyName,
                            date.attr("datetime"),
                            link,
                            hcp.retrieveDescription(link)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
