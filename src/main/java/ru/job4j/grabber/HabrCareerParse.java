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

    private String retrieveDescription(String link) throws IOException {
        Connection cn = Jsoup.connect(link);
        Document doc = cn.get();
        return doc.select(".style-ugc").text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> data = new ArrayList<>();
        for (int page = 1; page <= PAGE_NUM; page++) {
            int printPage = page;
            Connection connection = Jsoup.connect(link + page);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Post post = new Post();
                Element titleElement = row.select(".vacancy-card__title").first();
                post.setTitle(titleElement.text());
                post.setLink(SOURCE_LINK.concat(titleElement.child(0).attr("href")));
                post.setCreated(dateTimeParser
                        .parse(row.select(".vacancy-card__date")
                                .first()
                                .child(0)
                                .attr("datetime")));
                data.add(post);
                System.out.printf("%s%d%40s%d\r",
                        "Page: ",
                        printPage,
                        "Загрузка title, link, datetime: ",
                        data.size());
            });
        }
        int printNum = 1;
        for (Post post : data) {
            post.setDescription(retrieveDescription(post.getLink()));
            System.out.printf("%s%d\r",
                    "Загрузка description: ",
                    printNum++);
        }
        return data;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> vacancies = new ArrayList<>(hcp.list(PAGE_LINK));
        vacancies.forEach(System.out::println);
    }
}
