package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReviewStorageTest {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final User user1;
    private final User user2;
    private final Film film1;

    @Autowired
    public ReviewStorageTest(ReviewStorage reviewStorage, @Qualifier("userH2Storage") UserStorage userStorage, @Qualifier("filmH2Storage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.user1 = createUser1();
        this.user2 = createUser2();
        this.film1 = createFilm1();
    }

    @Test
    public void add_return_new_review() {
        //Arrange
        Review review1 = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        //Act
        Optional<Review> actual = reviewStorage.add(review1);

        //Assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("reviewId", 1)
                                .hasFieldOrPropertyWithValue("content", review1.getContent())
                                .hasFieldOrPropertyWithValue("userId", review1.getUserId())
                                .hasFieldOrPropertyWithValue("filmId", review1.getFilmId())
                                .hasFieldOrPropertyWithValue("isPositive", review1.getIsPositive())
                                .hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    public void update_return_updated_review() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review1 = reviewStorage.add(newReview).get();

        //Act
        Review updateReview = review1.toBuilder()
                .isPositive(false)
                .content("It's not so cool")
                .build();
        Optional<Review> actual = reviewStorage.update(updateReview);

        //Assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("reviewId", 1)
                                .hasFieldOrPropertyWithValue("content", updateReview.getContent())
                                .hasFieldOrPropertyWithValue("isPositive", updateReview.getIsPositive())
                                .hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    public void delete_return_optional_empty() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review = reviewStorage.add(newReview).get();

        //Act
        reviewStorage.deleteReview(review.getReviewId());
        Optional<Review> actual = reviewStorage.findReviewById(review.getReviewId());

        //Assert
        assertThat(actual).isEmpty();
    }

    @Test
    public void getAllReviewsByFilmId_return_review_list_by_film() {

        //Arrange
        Review review1 = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();

        Review review2 = Review.builder()
                .content("This is so cool too!")
                .filmId(film1.getId())
                .userId(user2.getId())
                .isPositive(true).build();

        reviewStorage.add(review1);
        reviewStorage.add(review2);

        //Act
        List<Review> actual = reviewStorage.getAllReviewsByFilmId(film1.getId(), 10);

        //Assert
        assertThat(actual)
                .asList()
                .hasSize(2);
    }

    @Test
    public void addAnyLike_is_like_review_useful_has_1() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review1 = reviewStorage.add(newReview).get();

        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(review1.getReviewId())
                .isLike(true)
                .userId(user2.getId()).build();

        //Act
        reviewStorage.addReviewLike(reviewLike);
        var actual = reviewStorage.findReviewById(review1.getReviewId());

        //Assert
        assertThat(actual).isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("useful", 1));
    }

    @Test
    public void addAnyLike_is_dislike_review_useful_has_minus_1() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review1 = reviewStorage.add(newReview).get();

        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(review1.getReviewId())
                .isLike(false)
                .userId(user2.getId()).build();

        //Act
        reviewStorage.addReviewLike(reviewLike);
        var actual = reviewStorage.findReviewById(review1.getReviewId());

        //Assert
        assertThat(actual).isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("useful", -1));
    }

    @Test
    public void removeAnyLike_review_useful_should_be_restored() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review1 = reviewStorage.add(newReview).get();

        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(review1.getReviewId())
                .isLike(true)
                .userId(user2.getId()).build();

        //Act
        reviewStorage.addReviewLike(reviewLike);
        reviewStorage.removeReviewLikeOrDislike(reviewLike);
        var actual = reviewStorage.findReviewById(review1.getReviewId());

        //Assert
        assertThat(actual).isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    public void removeDislike_review_useful_should_be_restored() {
        //Arrange
        Review newReview = Review.builder()
                .content("This is so cool!")
                .filmId(film1.getId())
                .userId(user1.getId())
                .isPositive(true).build();
        Review review1 = reviewStorage.add(newReview).get();

        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(review1.getReviewId())
                .isLike(false)
                .userId(user2.getId()).build();

        //Act
        reviewStorage.addReviewLike(reviewLike);
        reviewStorage.removeDislike(reviewLike);
        var actual = reviewStorage.findReviewById(review1.getReviewId());

        //Assert
        assertThat(actual).isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review)
                                .hasFieldOrPropertyWithValue("useful", 0));
    }

    private User createUser1() {
        User newUser = User.builder()
                .email("ivan@ya.ru")
                .login("ivan@ya.ru")
                .name("ivan")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private User createUser2() {
        User newUser = User.builder()
                .email("nikolay@ya.ru")
                .login("nikolay@ya.ru")
                .name("Nikolay")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        return userStorage.add(newUser);
    }

    private Film createFilm1() {
        Mpa mpa1 = Mpa.builder().id(1).build();
        Genre thriller = Genre.builder().id(4).build();

        Film newFilm = Film.builder()
                .name("Аватар")
                .description("3D")
                .duration(180)
                .releaseDate(LocalDate.of(2009, 12, 1))
                .mpa(mpa1)
                .build();
        newFilm.setGenres(Set.of(thriller));
        return filmStorage.add(newFilm);
    }
}