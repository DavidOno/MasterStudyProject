package de.smarthome.command;

import org.springframework.http.ResponseEntity;

public interface Request {

    public ResponseEntity execute();
}
