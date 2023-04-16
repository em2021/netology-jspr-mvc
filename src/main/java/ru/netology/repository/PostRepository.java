package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepository {

    private final ConcurrentHashMap<Long, String> repo = new ConcurrentHashMap<>();
    private final AtomicLong idCount = new AtomicLong();

    public List<Post> all() {
        List<Post> posts = new ArrayList<>();
        if (repo.size() > 0) {
            repo.entrySet().stream().iterator().forEachRemaining(s -> posts.add(new Post(s.getKey(), s.getValue())));
            return posts;
        }
        return posts;
    }

    public Optional<Post> getById(long id) {
        String content = repo.get(id);
        if (content != null) {
            return Optional.of(new Post(id, content));
        }
        return Optional.empty();
    }

    public Post save(Post post) {
        long id = post.getId();
        String content = post.getContent();
        long setId = -1;
        if (id == 0) {
            repo.put(setId = idCount.incrementAndGet(), content);
        }
        if (id != 0) {
            if (repo.containsKey(id)) {
                repo.put(id, content);
            } else {
                repo.putIfAbsent(id, content);
            }
        }
        if (setId > 0) {
            return getById(setId).get();
        }
        return post;
    }

    public void removeById(long id) {
        repo.remove(id);
    }
}
