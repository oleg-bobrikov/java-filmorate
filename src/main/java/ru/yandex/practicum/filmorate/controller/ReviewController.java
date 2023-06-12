package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.Film;
import ru.yandex.practicum.filmorate.dto.Review;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final FilmService filmService;

    @PostMapping()
    public Review add(@NotNull @Valid @RequestBody Review review) {
        return reviewService.add(review);
    }

    @PutMapping()
    public Review update(@NotNull @Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable @NotNull Integer id) {
        reviewService.removeReview(id);
    }

    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable @NotNull Integer id) {
        return reviewService.findReviewById(id);
    }

    @PutMapping("{reviewId}/like/{userId}")
    public void like(@PathVariable @NotNull Integer reviewId, @PathVariable @NotNull Integer userId) {
        reviewService.like(reviewId, userId);
    }

    @PutMapping("{reviewId}/dislike/{userId}")
    public void dislike(@PathVariable @NotNull Integer reviewId, @PathVariable @NotNull Integer userId) {
        reviewService.dislike(reviewId, userId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{reviewId}/like/{userId}")
    public void removeAnyLike(@PathVariable @NotNull Integer reviewId, @PathVariable @NotNull Integer userId) {
        reviewService.removeAnyLike(reviewId, userId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{reviewId}/dislike/{userId}")
    public void removeDislike(@PathVariable @NotNull Integer reviewId, @PathVariable @NotNull Integer userId) {
        reviewService.removeDislike(reviewId, userId);
    }

    @GetMapping("")
    public List<Review> getAll(@RequestParam(required = false) Optional<Integer> filmId,
                               @RequestParam(defaultValue = "10", required = false) Integer count) {
        if (filmId.isPresent()) {
            Film film = filmService.getFilmById(filmId.get());
            return reviewService.getAllReviewsByFilmId(film.getId(), count);
        } else {
            return reviewService.getAllReviews(count);
        }
    }
}
