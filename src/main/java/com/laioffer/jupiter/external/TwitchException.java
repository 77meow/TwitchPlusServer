package com.laioffer.jupiter.external;

public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        // Use parent class's constructor
        super(errorMessage);
    }
}