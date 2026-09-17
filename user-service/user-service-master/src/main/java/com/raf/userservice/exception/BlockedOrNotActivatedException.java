package com.raf.cinemauserservice.exception;

import org.springframework.http.HttpStatus;

public class BlockedOrNotActivatedException extends CustomException{

    public BlockedOrNotActivatedException(String message) {
        super(message, ErrorCode.BLOCKED_OR_NOT_ACTIVATED, HttpStatus.FORBIDDEN);
    }
}
