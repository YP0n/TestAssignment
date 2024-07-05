package ua.ypon.TestAssignment_Java.util;

/**
 * @author ua.ypon 04.07.2024
 */
public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
