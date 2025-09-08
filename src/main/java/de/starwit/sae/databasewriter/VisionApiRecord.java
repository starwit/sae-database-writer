package de.starwit.sae.databasewriter;

import java.time.ZonedDateTime;

import de.starwit.visionapi.Common.MessageType;

public record VisionApiRecord(ZonedDateTime timestamp, String streamKey, MessageType messageType, String protoJson) {}
