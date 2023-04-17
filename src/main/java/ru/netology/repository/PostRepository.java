package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PostRepository {

    private final ConcurrentHashMap<Long, Post> repo = new ConcurrentHashMap<>();
    private final AtomicLong idCount = new AtomicLong();

    public List<Post> all() {
        List<Post> posts = new ArrayList<>();
        if (repo.size() > 0) {
            posts = repo.values().stream().filter(s -> !s.isRemoved()).collect(Collectors.toList());
        }
        return posts;
    }

    public Optional<Post> getById(long id) {
        if (repo.size() > 0 && repo.get(id) != null) {
            Optional<Post> post = Optional.of(repo.get(id));
            if (!post.isPresent() || (post.isPresent() && post.get().isRemoved())) {
                throw new NotFoundException();
            }
            return post;
        }
        return Optional.empty();
    }

    public Post save(Post post) {
        long id = post.getId();
        String content = post.getContent();
        long setId = -1;
        if (id == 0) {
            //checks if repo contains a post with the new id
            try {
                if (!getById(idCount.get() + 1).isEmpty()) {
                    //increments idCount until repo has no posts associated with the new id
                    while (getById(idCount.get() + 1).isPresent()) {
                        idCount.incrementAndGet();
                    }
                }
            } catch (NotFoundException e) {

            }
            //assigns a new id to post and saves it to repo
            post = new Post(setId = idCount.incrementAndGet(), content);
            repo.put(setId, post);
        }
        if (id != 0) {
            if (repo.containsKey(id) && !repo.get(id).isRemoved()) {
                //repo contains a post with given id & it's not removed - existing post is updated
                repo.get(id).setContent(content);
                post = getById(id).get();
            } else if (repo.containsKey(id) && repo.get(id).isRemoved()) {
                //repo contains a post with given id & it's removed - existing post is not updated
                throw new NotFoundException();
            } else {
                //no post with given id in repo - post is saved to repo as is
                post = new Post(id, content);
                repo.putIfAbsent(id, post);
            }
        }
        return post;
    }

    public void removeById(long id) {
        repo.get(id).setRemoved();
    }
}