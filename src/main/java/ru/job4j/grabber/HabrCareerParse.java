package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int PAGE_NUM = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        Connection cn = Jsoup.connect(link);
        Document doc;
        try {
            doc = cn.get();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return doc.select(".style-ugc").text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> data = new ArrayList<>();
        for (int page = 1; page <= PAGE_NUM; page++) {
            Connection connection = Jsoup.connect(link + page);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
            Elements elements = document.select(".vacancy-card__inner");
            elements.forEach(el -> data.add(parsePost(el)));
        }
        return data;
    }

    @Override
    public Post parsePost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        String link = SOURCE_LINK.concat(titleElement.child(0).attr("href"));
        return new Post(titleElement.text(),
                link,
                retrieveDescription(link),
                dateTimeParser
                        .parse(element.select(".vacancy-card__date")
                                .first()
                                .child(0)
                                .attr("datetime"))
        );
    }

    public static void main(String[] args) {
        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> vacancies = new ArrayList<>(hcp.list(PAGE_LINK));
        vacancies.forEach(System.out::println);
    }
}
