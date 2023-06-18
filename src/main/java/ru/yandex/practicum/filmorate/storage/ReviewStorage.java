package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review add(Review review);

    Optional<Review> findReviewById(Integer id);

    Optional<ReviewLike> findReviewLikeOrDislike(Integer id, Integer userId);

    Review update(Review review);

    void deleteReview(int id);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsByFilmId(Integer filmId, Integer count);

    void addReviewLike(ReviewLike reviewLike);

    void removeReviewLikeOrDislike(ReviewLike reviewLike);
}
