package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
class UserStorageTest {
    @Autowired
    @Qualifier("userH2Storage")
    private UserStorage userStorage;

    @Test
    public void findUserById_returnUser_userExists() {
        //arrange
        User testedUser = User.builder()
                .email("ivan@ya.ru")
                .login("ivan@ya.ru")
                .name("ivan")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.add(testedUser);
        testedUser.setId(createdUser.getId());

        //act
        Optional<User> actual = userStorage.findUserById(testedUser.getId());

        //assert
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .hasFieldOrPropertyWithValue("id", testedUser.getId())
                                .hasFieldOrPropertyWithValue("email", testedUser.getEmail())
                                .hasFieldOrPropertyWithValue("login", testedUser.getLogin())
                                .hasFieldOrPropertyWithValue("name", testedUser.getName())
                                .hasFieldOrPropertyWithValue("birthday", testedUser.getBirthday())
                );
    }

    @Test
    public void findUserById_returnEmpty_userUnknown() {
        //arrange
        int unknownId = -1;

        //act
        Optional<User> actual = userStorage.findUserById(unknownId);

        //assert
        assertThat(actual).isEmpty();
    }

    @Test
    public void update_returnUpdatedUser() {
        //arrange
        User testUser = User.builder()
                .email("sergey@ya.ru")
                .login("sergey@ya.ru")
                .name("Sergey")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.add(testUser);
        testUser.setId(createdUser.getId());

        testUser.setEmail("sergey2@ya.ru");
        testUser.setLogin("sergey2@ya.ru");
        testUser.setName("Sergey2");
        testUser.setBirthday(LocalDate.of(2000, 2, 1));

        //act
        userStorage.update(testUser);
        Optional<User> userOptional = userStorage.findUserById(testUser.getId());

        //assert
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .hasFieldOrPropertyWithValue("id", testUser.getId())
                                .hasFieldOrPropertyWithValue("email", testUser.getEmail())
                                .hasFieldOrPropertyWithValue("login", testUser.getLogin())
                                .hasFieldOrPropertyWithValue("name", testUser.getName())
                                .hasFieldOrPropertyWithValue("birthday", testUser.getBirthday())
                );
    }


    @Test
    void deleteUserById_deleteUser_userExists() {
        //arrange
        User testUser = User.builder()
                .email("igor@ya.ru")
                .login("igor@ya.ru")
                .name("Igor")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.add(testUser);
        int userId = createdUser.getId();
        testUser.setId(userId);

        //act
        userStorage.deleteUserById(userId);
        Optional<User> actual = userStorage.findUserById(userId);

        //assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getUserFriends_returnUserFriends() {
        //arrange
        User user = User.builder()
                .email("maxim@ya.ru")
                .login("maxim@ya.ru")
                .name("Maxim")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.add(user);

        User friend1 = User.builder()
                .email("julia@ya.ru")
                .login("julia@ya.ru")
                .name("Julia")
                .birthday(LocalDate.of(2003, 11, 1))
                .build();
        User createdFriend1 = userStorage.add(friend1);
        userStorage.addFriend(createdUser, createdFriend1);

        User friend2 = User.builder()
                .email("eugenia@ya.ru")
                .login("eugenia@ya.ru")
                .name("Eugenia")
                .birthday(LocalDate.of(2003, 5, 1))
                .build();
        User createdFriend2 = userStorage.add(friend2);
        userStorage.addFriend(createdUser, createdFriend2);

        //act
        List<User> actual = userStorage.getUserFriendsById(createdUser.getId());

        //assert
        assertThat(actual)
                .asList()
                .hasSize(2)
                .contains(createdFriend1, createdFriend2);
    }

   @Test
    void getUsers_returnUsers() {
        //arrange
        User user1 = User.builder()
                .email("albert@ya.ru")
                .login("albert@ya.ru")
                .name("Albert")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser1 = userStorage.add(user1);

        User user2 = User.builder()
                .email("nikolay@ya.ru")
                .login("nikolay@ya.ru")
                .name("Nikolay")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser2 = userStorage.add(user2);

        //act
        List<User> actual = userStorage.getUsers();

        //assert
        assertThat(actual)
                .asList()
                .contains(createdUser1, createdUser2);
    }

    @Test
    void removeFriend_deleteFriendFromUser() {
        //arrange
        User user = User.builder()
                .email("egor@ya.ru")
                .login("egor@ya.ru")
                .name("Egor")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.add(user);

        User friend = User.builder()
                .email("svyatoslav@ya.ru")
                .login("svyatoslav@ya.ru")
                .name("Svyatoslav")
                .birthday(LocalDate.of(2003, 11, 1))
                .build();

        User createdFriend = userStorage.add(friend);
        userStorage.addFriend(createdUser, createdFriend);
        userStorage.removeFriend(createdUser, createdFriend);

        //act
        List<User> actual = userStorage.getUserFriendsById(createdUser.getId());

        //assert
        assertThat(actual)
                .asList()
                .isEmpty();
    }
}