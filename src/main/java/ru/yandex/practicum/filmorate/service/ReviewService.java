package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public Review add(Review review) {
        userService.findUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        findReviewById(review.getReviewId());
        return reviewStorage.update(review);
    }

    public Review findReviewById(Integer id) {

        return reviewStorage.findReviewById(id).orElseThrow(
                () -> new NotFoundException("Отзыв с идентификатором " + id + " не найден."));
    }

    public void removeReview(Integer id) {
        Optional<Review> reviewOptional = reviewStorage.findReviewById(id);
        if (reviewOptional.isEmpty()) {
            throw new NotFoundException("Отзыв с идентификатором " + id + " не найден.");
        }
        reviewStorage.deleteReview(id);
    }

    public void addReviewLike(int reviewId, int userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(true)
                .build();
        reviewStorage.addReviewLike(reviewLike);
    }

    public void addReviewDislike(int reviewId, int userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike newLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(false)
                .build();
        reviewStorage.addReviewLike(newLike);
    }

    public void removeReviewLikeOrDislike(Integer reviewId, Integer userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = reviewStorage.findReviewLikeOrDislike(reviewId, userId).orElseThrow(() ->
                new NotFoundException(String.format("Лайк на с reviewId = %s и userId=%s не найден", reviewId, userId)));

        reviewStorage.removeReviewLikeOrDislike(reviewLike);
    }

    public void removeReviewDislike(Integer reviewId, Integer userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = reviewStorage.findReviewLikeOrDislike(reviewId, userId).orElseThrow(() ->
                new NotFoundException(String.format("Дизлайк с reviewId = %s и userId=%s не найден", reviewId, userId)));
        if (reviewLike.isLike()) {
            throw new NotFoundException(String.format("Дизлайк с reviewId = %s и userId=%s не найден", reviewId, userId));
        }
        reviewStorage.removeReviewLikeOrDislike(reviewLike);
    }

    public List<Review> getAllReviews(Integer count) {
        return reviewStorage.getAllReviews(count);
    }

    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        filmService.getFilmById(filmId);
        return reviewStorage.getAllReviewsByFilmId(filmId, count);
    }

    public Map<String, Object> reviewToMap(Review review) {
        Map<String, Object> params = new HashMap<>();
        params.put("ID", review.getReviewId());
        params.put("CONTENT", review.getContent());
        params.put("IS_POSITIVE", review.getIsPositive());
        params.put("USER_ID", review.getUserId());
        params.put("FILM_ID", review.getFilmId());
        params.put("USEFUL", review.getUseful());
        return params;
    }

    public Map<String, Object> reviewLiketoMap(ReviewLike reviewLike) {
        Map<String, Object> params = new HashMap<>();
        params.put("REVIEW_ID", reviewLike.getReviewId());
        params.put("USER_ID", reviewLike.getUserId());
        params.put("IS_LIKE", reviewLike.isLike());
        return params;
    }
}
