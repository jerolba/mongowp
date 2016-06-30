/*
 * This file is part of MongoWP.
 *
 * MongoWP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MongoWP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with v3m0. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2016 8Kdata.
 * 
 */

package com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic;

import com.eightkdata.mongowp.bson.BsonArray;
import com.eightkdata.mongowp.bson.BsonDocument;
import com.eightkdata.mongowp.bson.BsonValue;
import com.eightkdata.mongowp.exceptions.*;
import com.eightkdata.mongowp.fields.ArrayField;
import com.eightkdata.mongowp.fields.DoubleField;
import com.eightkdata.mongowp.fields.StringField;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic.GetLogCommand.GetLogArgument;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic.GetLogCommand.GetLogReply;
import com.eightkdata.mongowp.server.api.MarshalException;
import com.eightkdata.mongowp.server.api.impl.AbstractCommand;
import com.eightkdata.mongowp.utils.BsonArrayBuilder;
import com.eightkdata.mongowp.utils.BsonDocumentBuilder;
import com.eightkdata.mongowp.utils.BsonReaderTool;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 */
public class GetLogCommand extends AbstractCommand<GetLogArgument, GetLogReply> {

    private static final String COMMAND_NAME = "getLog";
    public static final GetLogCommand INSTANCE = new GetLogCommand();

    public GetLogCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isSlaveOk() {
        return true;
    }

    @Override
    public Class<? extends GetLogArgument> getArgClass() {
        return GetLogArgument.class;
    }

    @Override
    public GetLogArgument unmarshallArg(BsonDocument requestDoc) throws BadValueException,
            TypesMismatchException, NoSuchKeyException, FailedToParseException {
        return GetLogArgument.unmarshall(requestDoc);
    }

    @Override
    public BsonDocument marshallArg(GetLogArgument request) throws MarshalException {
        return request.marshall();
    }

    @Override
    public Class<? extends GetLogReply> getResultClass() {
        return GetLogReply.class;
    }

    @Override
    public GetLogReply unmarshallResult(BsonDocument resultDoc) throws BadValueException,
            TypesMismatchException, NoSuchKeyException, FailedToParseException, MongoException {
        try {
            return LogGetLogReply.unmarshall(resultDoc);
        } catch (NoSuchKeyException ex) {
            return AsteriskGetLogReply.unmarshall(resultDoc);
        }
    }

    @Override
    public BsonDocument marshallResult(GetLogReply result) throws MarshalException {
        return result.marshall();
    }

    public static class GetLogArgument {
        private static final StringField COMMAND_NAME_FIELD = new StringField(COMMAND_NAME);
        private final boolean isAsterisk;
        private final String logName;

        public GetLogArgument(boolean isAsterisk, String logName) {
            this.isAsterisk = isAsterisk;
            this.logName = logName;
        }

        public boolean isIsAsterisk() {
            return isAsterisk;
        }

        @Nonnull
        public String getLogName() {
            return logName;
        }

        private static GetLogArgument unmarshall(BsonDocument resultDoc) throws TypesMismatchException, NoSuchKeyException, BadValueException {
            String logName = BsonReaderTool.getString(resultDoc, COMMAND_NAME_FIELD);

            return new GetLogArgument(logName.equals("*"), logName);
        }

        BsonDocument marshall() {
            return new BsonDocumentBuilder(1)
                    .append(COMMAND_NAME_FIELD, logName)
                    .build();
        }
    }
    
    public static abstract class GetLogReply {
        abstract BsonDocument marshall();
    }

    public static class LogGetLogReply extends GetLogReply {
        private final int totalLines;
        private final Iterable<String> lines;

        private static final DoubleField TOTAL_LINES_FIELD = new DoubleField("totalLinesWritten");
        private static final ArrayField LOG_FIELD = new ArrayField("log");

        public LogGetLogReply(int totalLines, Iterable<String> lines) {
            this.totalLines = totalLines;
            this.lines = lines;
        }

        private static GetLogReply unmarshall(BsonDocument resultDoc) throws TypesMismatchException, NoSuchKeyException, BadValueException {
            int totalLines = BsonReaderTool.getNumeric(resultDoc, TOTAL_LINES_FIELD).intValue();
            
            BsonArray array = BsonReaderTool.getArray(resultDoc, LOG_FIELD);
            List<String> lines = new ArrayList<>(array.size());

            for (BsonValue<?> bsonValue : array) {
                if (!bsonValue.isString()) {
                    throw new BadValueException("The argument " + LOG_FIELD.getFieldName()
                            + " should only contain strings, but a " + bsonValue.getValue()
                            + " was found");
                }
                lines.add(bsonValue.asString().getValue());
            }

            return new LogGetLogReply(totalLines, lines);
        }

        public int getTotalLines() {
            return totalLines;
        }

        public Iterable<String> getLines() {
            return lines;
        }

        @Override
        BsonDocument marshall() {
            BsonDocumentBuilder builder = new BsonDocumentBuilder(2);
            builder.append(TOTAL_LINES_FIELD, totalLines);

            BsonArrayBuilder arrayBuilder = new BsonArrayBuilder(totalLines);
            for (String line : lines) {
                arrayBuilder.add(line);
            }

            builder.append(LOG_FIELD, arrayBuilder.build());

            return builder.build();
        }
    }

    public static class AsteriskGetLogReply extends GetLogReply {

        public static final ArrayField NAMES_FIELD = new ArrayField("names");
        private final List<String> names;

        public AsteriskGetLogReply(List<String> names) {
            this.names = names;
        }

        private static GetLogReply unmarshall(BsonDocument resultDoc) throws TypesMismatchException, NoSuchKeyException, BadValueException {
            BsonArray array = BsonReaderTool.getArray(resultDoc, NAMES_FIELD);
            List<String> names = new ArrayList<>(array.size());

            for (BsonValue<?> bsonValue : array) {
                if (!bsonValue.isString()) {
                    throw new BadValueException("The argument " + NAMES_FIELD.getFieldName()
                            + " should only contain strings, but a " + bsonValue.getValue()
                            + " was found");
                }
                names.add(bsonValue.asString().getValue());
            }

            return new AsteriskGetLogReply(names);
        }

        @Override
        BsonDocument marshall() {
            BsonArrayBuilder arrayBuilder = new BsonArrayBuilder(names.size());
            for (String name : names) {
                arrayBuilder.add(name);
            }

            return new BsonDocumentBuilder(1)
                    .append(NAMES_FIELD, arrayBuilder.build())
                    .build();
        }

    }
}