package ru.job4j.grabber;

import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.List;

public interface Parse {
    List<Post> list(String link) throws IOException;
    Post parsePost(Element element);
}