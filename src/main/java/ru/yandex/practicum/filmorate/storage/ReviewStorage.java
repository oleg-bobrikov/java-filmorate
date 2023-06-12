package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.Review;
import ru.yandex.practicum.filmorate.dto.ReviewLike;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Optional<Review> add(Review review);

    Optional<Review> findReviewById(Integer id);

    Optional<Review> update(Review review);

    void deleteReview(int id);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsByFilmId(Integer filmId, Integer count);

    void addAnyLike(ReviewLike reviewLike);

    void removeAnyLike(ReviewLike reviewLike);

    void removeDislike(ReviewLike reviewLike);

}
