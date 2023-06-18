package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.exception.DataBaseException;
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
        Optional<Review> reviewOpt = reviewStorage.add(review);
        if (reviewOpt.isPresent()) {
            return reviewOpt.get();
        } else {
            throw new DataBaseException("Ошибка получения отзыва по идентификатору " + review.getReviewId());
        }

    }

    public Review update(Review review) {
        findReviewById(review.getReviewId());
        Optional<Review> reviewOpt = reviewStorage.update(review);
        if (reviewOpt.isPresent()) {
            return reviewOpt.get();
        } else {
            throw new DataBaseException("Ошибка получения отзыва по идентификатору " + review.getReviewId());
        }
    }

    public Review findReviewById(Integer id) {
        Optional<Review> reviewOptional = reviewStorage.findReviewById(id);
        if (reviewOptional.isEmpty()) {
            throw new NotFoundException("Отзыв с идентификатором " + id + " не найден.");
        }

        return reviewOptional.get();
    }

    public void removeReview(Integer id) {
        Optional<Review> reviewOptional = reviewStorage.findReviewById(id);
        if (reviewOptional.isEmpty()) {
            throw new NotFoundException("Отзыв с идентификатором " + id + " не найден.");
        }
        reviewStorage.deleteReview(id);
    }

    public void like(int reviewId, int userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(true)
                .build();
        reviewStorage.addAnyLike(reviewLike);
    }

    public void dislike(int reviewId, int userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike newLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(false)
                .build();
        reviewStorage.addAnyLike(newLike);
    }

    public void removeAnyLike(Integer reviewId, Integer userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(false)
                .build();
        reviewStorage.removeAnyLike(reviewLike);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        findReviewById(reviewId);
        userService.findUserById(userId);
        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isLike(false)
                .build();
        reviewStorage.removeDislike(reviewLike);
    }

    public List<Review> getAllReviews(Integer count) {
        return reviewStorage.getAllReviews(count);
    }

    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
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
