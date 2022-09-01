package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.quartz.AlertRabbit;
import java.sql.*;
import java.util.*;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("grabber.driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("grabber.url"),
                    cfg.getProperty("grabber.username"),
                    cfg.getProperty("grabber.password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement("INSERT INTO post(name, text, link, created) values(?, ?, ?, ?)"
                        + "ON CONFLICT(link) DO NOTHING;",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet rslSet = ps.getGeneratedKeys()) {
                if (rslSet.next()) {
                    post.setId(rslSet.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> data = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement("SELECT * FROM post;")) {
            try (ResultSet rslSet = ps.executeQuery()) {
                while (rslSet.next()) {
                    data.add(rslSetToPost(rslSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        try (PreparedStatement ps = cnn.prepareStatement("SELECT * FROM post WHERE id = ?;")) {
            ps.setInt(1, id);
            try (ResultSet rslSet = ps.executeQuery()) {
                if (rslSet.next()) {
                    rsl = rslSetToPost(rslSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post rslSetToPost(ResultSet rslSet) throws SQLException {
        return new Post(
                rslSet.getInt("id"),
                rslSet.getString("name"),
                rslSet.getString("link"),
                rslSet.getString("text"),
                rslSet.getTimestamp("created").toLocalDateTime()
        );
    }

    public static void main(String[] args) {
        final String SOURCE_LINK = "https://career.habr.com";
        final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
        final String rowSeparator = "-".repeat(30);
        boolean exitAction = false;

        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());

        try (PsqlStore psStore = new PsqlStore(AlertRabbit.loadProperties("rabbit.properties"))) {
            System.out.println("Парсинг вакансий с сайта и их запись в коллекцию List " + SOURCE_LINK + "...");
            List<Post> vacancies = new ArrayList<>(hcp.list(PAGE_LINK));
            System.out.printf("Готово!%n%s%n", rowSeparator);
            System.out.println("Сохранение записей коллекции List в таблице базы данных post...");
            vacancies.forEach(psStore::save);
            System.out.printf("Готово!%n%s%n", rowSeparator);
            System.out.println("Вывод в консоль записей таблицы базы данных post...");
            new ArrayList<>(psStore.getAll()).forEach(System.out::println);
            System.out.printf("Готово!%n%s%n", rowSeparator);
            System.out.println("Проверка поиска записи по id...");
            Scanner in = new Scanner(System.in);
            while (!exitAction) {
                System.out.print("Введите id или \"exit\": ");
                var val = in.next();
                if ("exit".equals(val)) {
                    exitAction = true;
                    continue;
                }
                System.out.printf("%s%n%s%n", psStore.findById(Integer.parseInt(val)), rowSeparator);
            }
            System.out.println("Работа завершена.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}