package lt.idomus.takas.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.idomus.takas.constant.NameConstants;
import lt.idomus.takas.dto.CreateUserDTO;
import lt.idomus.takas.exceptions.exception.CustomMessage;
import lt.idomus.takas.model.ArticleUser;
import lt.idomus.takas.model.JwtLoginSuccessResponse;
import lt.idomus.takas.model.LoginRequest;
import lt.idomus.takas.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/user")
@Slf4j
public class UserController {


    public static final String RESPONSE_OK = new CustomMessage("Favorites updated!").json();
    private final UserService userService;
    public static final String RESPONSE_ERROR = new CustomMessage("Error user or article not found!").json();


    /***
     *  Here both oauth2 and classic login are being processed
     * @param loginRequest .json data sent to server to be validated
     * @return if user is authenticated return user data
     *          else display error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest
    ) {

        JwtLoginSuccessResponse jwtResponse = userService.loginAttempt(loginRequest);
        if (jwtResponse.getMessage().equals(NameConstants.LOGIN_UNAUTHENTICATED)) {
            var response = new CustomMessage(NameConstants.LOGIN_UNAUTHENTICATED);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Endpoint for new user registration
     *
     * @param userForm user .json POST data with user credentials
     * @return .json success 'message' if data is @Valid else provide error 'message'
     */

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO userForm) {
        log.debug("User form: " + userForm);
        CreateUserDTO user = userService.createUser(userForm, false);
        log.debug("Registered user: " + user);
        return new ResponseEntity<>(new CustomMessage(NameConstants.REGISTRATION_SUCCESSFUL).json(), HttpStatus.CREATED);
    }


    /**
     * Favorite article item
     *
     * @param articleID      id you want to add to favorite list
     * @param authentication
     * @return if successful return .json string with http ok status, else return not found
     */
    @PostMapping("/favorite/{articleID}")
    public ResponseEntity<?> addToFavorites(@PathVariable Long articleID, @ApiIgnore Authentication authentication) {
        var userFavorites = userService.favoritesAdd(articleID, authentication, false);
        if (userFavorites.isStatus()) {
            return new ResponseEntity<>(userFavorites, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(userFavorites, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Remove from favorites article item
     *
     * @param articleID      id you want to add to favorite list
     * @param authentication
     * @return if successful return .json string with http ok status, else return not found
     */
    @DeleteMapping("/favorite/{articleID}")
    public ResponseEntity<?> deleteFromFavorites(@PathVariable Long articleID, @ApiIgnore Authentication authentication) {

        CustomMessage<?> userFavorites = userService.favoritesAdd(articleID, authentication, true);
        if (userFavorites.isStatus()) {
            return new ResponseEntity<>(userFavorites, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(userFavorites, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Returns user details
     *
     * @param authentication user authentication in context
     * @return If user is authenticated return user details
     * else display error message
     */
    @GetMapping("/details")
    public ResponseEntity<?> getUserInfo(@ApiIgnore Authentication authentication) {

        if (authentication == null) {
            var response = new CustomMessage<>("No authentication detected");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Optional<ArticleUser> userDetails = userService.getUserDetails(authentication);
        if (userDetails.isEmpty()) {
            var response = new CustomMessage<>("User nor found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(userDetails);

    }

    /**
     *  Get user data base on userid
     * @param userId user id
     * @return filtered used data
     */
    @GetMapping("/public/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        CustomMessage<Object> response = userService.getUser(userId);
        if (response.isStatus()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
