/*
 * MongoWP
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.eightkdata.mongowp.bson.org.bson.utils;

import com.eightkdata.mongowp.bson.BinarySubtype;
import com.eightkdata.mongowp.bson.BsonDocument.Entry;
import com.eightkdata.mongowp.bson.BsonRegex.Options;
import com.eightkdata.mongowp.bson.impl.ByteArrayBsonBinary;
import com.eightkdata.mongowp.bson.impl.ByteArrayBsonObjectId;
import com.eightkdata.mongowp.bson.impl.DefaultBsonDbPointer;
import com.eightkdata.mongowp.bson.impl.DefaultBsonJavaScript;
import com.eightkdata.mongowp.bson.impl.DefaultBsonJavaScriptWithCode;
import com.eightkdata.mongowp.bson.impl.DefaultBsonRegex;
import com.eightkdata.mongowp.bson.impl.DefaultBsonTimestamp;
import com.eightkdata.mongowp.bson.impl.FalseBsonBoolean;
import com.eightkdata.mongowp.bson.impl.ListBsonArray;
import com.eightkdata.mongowp.bson.impl.LongBsonDateTime;
import com.eightkdata.mongowp.bson.impl.MapBasedBsonDocument;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonDecimal128;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonDouble;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonInt32;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonInt64;
import com.eightkdata.mongowp.bson.impl.SimpleBsonMax;
import com.eightkdata.mongowp.bson.impl.SimpleBsonMin;
import com.eightkdata.mongowp.bson.impl.SimpleBsonNull;
import com.eightkdata.mongowp.bson.impl.SimpleBsonUndefined;
import com.eightkdata.mongowp.bson.impl.StringBsonString;
import com.eightkdata.mongowp.bson.impl.TrueBsonBoolean;
import com.google.common.primitives.UnsignedBytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonMaxKey;
import org.bson.BsonMinKey;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MongoBsonTranslator {

  private static final Logger LOGGER = LogManager.getLogger(MongoBsonTranslator.class);

  public static final Function<BsonDocument, com.eightkdata.mongowp.bson.BsonDocument> 
      FROM_MONGO_FUNCTION = new FromMongoFunction();
  public static final Function<com.eightkdata.mongowp.bson.BsonDocument, BsonDocument> 
      TO_MONGO_FUNCTION = new ToMongoFunction();
  private static final byte FIRST_USER_DEFINED = UnsignedBytes.parseUnsignedByte("80", 16);

  @Nullable
  public static BsonDocument translate(
      @Nullable com.eightkdata.mongowp.bson.BsonDocument wpDocument) throws IOException {
    return (BsonDocument) translatePrivate(wpDocument);
  }

  @Nullable
  public static com.eightkdata.mongowp.bson.BsonDocument translate(
      @Nullable org.bson.BsonDocument mongoDocument) {
    return (com.eightkdata.mongowp.bson.BsonDocument) translatePrivate(mongoDocument);
  }

  @Nullable
  private static BsonValue translatePrivate(
      @Nullable com.eightkdata.mongowp.bson.BsonValue<?> value) throws IOException {
    if (value == null) {
      return null;
    }

    switch (value.getType()) {
      case ARRAY: {
        com.eightkdata.mongowp.bson.BsonArray casted
            = (com.eightkdata.mongowp.bson.BsonArray) value;
        List<BsonValue> list = new ArrayList<>();
        for (com.eightkdata.mongowp.bson.BsonValue<?> wpValue : casted) {
          list.add(translatePrivate(wpValue));
        }
        return new BsonArray(list);
      }
      case BINARY: {
        com.eightkdata.mongowp.bson.BsonBinary casted =
            (com.eightkdata.mongowp.bson.BsonBinary) value;
        return new BsonBinary(casted.getNumericSubType(), casted.getByteSource().read());
      }
      case DATETIME:
        return new BsonDateTime(((com.eightkdata.mongowp.bson.BsonDateTime) value)
            .getMillisFromUnix());
      case DB_POINTER: {
        com.eightkdata.mongowp.bson.BsonDbPointer casted =
            (com.eightkdata.mongowp.bson.BsonDbPointer) value;
        return new BsonDbPointer(casted.getNamespace(), translateObjectId(casted.getId()));
      }
      case DOCUMENT: {
        com.eightkdata.mongowp.bson.BsonDocument casted =
            (com.eightkdata.mongowp.bson.BsonDocument) value;
        BsonDocument result = new org.bson.BsonDocument();
        for (Entry<?> entry : casted) {
          result.append(entry.getKey(), translatePrivate(entry.getValue()));
        }
        return result;
      }
      case DOUBLE:
        return new BsonDouble(((com.eightkdata.mongowp.bson.BsonDouble) value).doubleValue());
      case BOOLEAN: {
        com.eightkdata.mongowp.bson.BsonBoolean casted =
            (com.eightkdata.mongowp.bson.BsonBoolean) value;
        if (casted.getPrimitiveValue()) {
          return BsonBoolean.TRUE;
        } else {
          return BsonBoolean.FALSE;
        }
      }
      case INT32:
        return new BsonInt32(((com.eightkdata.mongowp.bson.BsonInt32) value).intValue());
      case INT64:
        return new BsonInt64(((com.eightkdata.mongowp.bson.BsonInt64) value).longValue());
      case JAVA_SCRIPT: {
        return new BsonJavaScript(((com.eightkdata.mongowp.bson.BsonJavaScript) value).getValue());
      }
      case JAVA_SCRIPT_WITH_SCOPE: {
        com.eightkdata.mongowp.bson.BsonJavaScriptWithScope casted =
            (com.eightkdata.mongowp.bson.BsonJavaScriptWithScope) value;
        return new BsonJavaScriptWithScope(casted.getJavaScript(), translate(casted.getScope()));
      }
      case MAX:
        return new BsonMaxKey();
      case MIN:
        return new BsonMinKey();
      case NULL:
        return new BsonNull();
      case OBJECT_ID:
        return new BsonObjectId(
            translateObjectId((com.eightkdata.mongowp.bson.BsonObjectId) value));
      case REGEX: {
        com.eightkdata.mongowp.bson.BsonRegex casted =
            (com.eightkdata.mongowp.bson.BsonRegex) value;
        return new BsonRegularExpression(casted.getPattern(), casted.getOptionsAsText());
      }
      case STRING:
        return new BsonString(((com.eightkdata.mongowp.bson.BsonString) value).getValue());
      case TIMESTAMP: {
        com.eightkdata.mongowp.bson.BsonTimestamp casted =
            (com.eightkdata.mongowp.bson.BsonTimestamp) value;
        return new BsonTimestamp(casted.getSecondsSinceEpoch(), casted.getOrdinal());
      }
      case UNDEFINED:
        return new BsonUndefined();
      case DEPRECATED:
      default:
        throw new AssertionError("It is not defined how to translate the type " + value.getType());
    }
  }

  @Nullable
  private static com.eightkdata.mongowp.bson.BsonValue<?> translatePrivate(
      @Nullable BsonValue value) {
    if (value == null) {
      return null;
    }
    switch (value.getBsonType()) {
      case ARRAY: {
        List<com.eightkdata.mongowp.bson.BsonValue<?>> list = new ArrayList<>();
        for (BsonValue mongoValue : value.asArray()) {
          list.add(translatePrivate(mongoValue));
        }
        return new ListBsonArray(list);
      }
      case BINARY: {
        BsonBinary casted = value.asBinary();
        return new ByteArrayBsonBinary(
            getBinarySubtype(casted.getType()),
            casted.getType(),
            casted.getData()
        );
      }
      case DATE_TIME:
        return new LongBsonDateTime(value.asDateTime().getValue());
      case DB_POINTER: {
        BsonDbPointer casted = value.asDBPointer();
        return new DefaultBsonDbPointer(casted.getNamespace(), new ByteArrayBsonObjectId(casted
            .getId().toByteArray()));
      }
      case DOCUMENT: {
        LinkedHashMap<String, com.eightkdata.mongowp.bson.BsonValue<?>> map = new LinkedHashMap<>(
            value.asDocument().size());
        for (java.util.Map.Entry<String, BsonValue> entry : value.asDocument().entrySet()) {
          map.put(entry.getKey(), translatePrivate(entry.getValue()));
        }
        return new MapBasedBsonDocument(map);
      }
      case DOUBLE:
        return PrimitiveBsonDouble.newInstance(value.asDouble().getValue());
      case BOOLEAN:
        if (value.asBoolean().getValue()) {
          return TrueBsonBoolean.getInstance();
        } else {
          return FalseBsonBoolean.getInstance();
        }
      case INT32:
        return PrimitiveBsonInt32.newInstance(value.asInt32().getValue());
      case INT64:
        return PrimitiveBsonInt64.newInstance(value.asInt64().getValue());
      case DECIMAL128:
          return PrimitiveBsonDecimal128.newInstance(value.asDecimal128().getValue().bigDecimalValue().floatValue());
      case JAVASCRIPT: {
        return new DefaultBsonJavaScript(value.asJavaScript().getCode());
      }
      case JAVASCRIPT_WITH_SCOPE: {
        BsonJavaScriptWithScope casted = value.asJavaScriptWithScope();
        return new DefaultBsonJavaScriptWithCode(casted.getCode(), translate(casted.getScope()));
      }
      case MAX_KEY:
        return SimpleBsonMax.getInstance();
      case MIN_KEY:
        return SimpleBsonMin.getInstance();
      case NULL:
        return SimpleBsonNull.getInstance();
      case OBJECT_ID:
        return new ByteArrayBsonObjectId(value.asObjectId().getValue().toByteArray());
      case REGULAR_EXPRESSION: {
        BsonRegularExpression casted = value.asRegularExpression();
        return new DefaultBsonRegex(parseRegexOptions(casted.getOptions()), casted.getPattern());
      }
      case STRING:
        return new StringBsonString(value.asString().getValue());
      case TIMESTAMP: {
        BsonTimestamp casted = value.asTimestamp();
        return new DefaultBsonTimestamp(casted.getTime(), casted.getInc());
      }
      case UNDEFINED:
        return SimpleBsonUndefined.getInstance();
      case END_OF_DOCUMENT:
      case SYMBOL:
      default:
        throw new AssertionError("It is not defined how to translate the type " + value
            .getBsonType());
    }
  }

  private static ObjectId translateObjectId(com.eightkdata.mongowp.bson.BsonObjectId id) {
    return new ObjectId(id.toHexString());
  }

  protected static BinarySubtype getBinarySubtype(byte readByte) {
    switch (readByte) {
      case 0x00:
        return BinarySubtype.GENERIC;
      case 0x01:
        return BinarySubtype.FUNCTION;
      case 0x02:
        return BinarySubtype.OLD_BINARY;
      case 0x03:
        return BinarySubtype.OLD_UUID;
      case 0x04:
        return BinarySubtype.UUID;
      case 0x05:
        return BinarySubtype.MD5;
      default: {
        if (UnsignedBytes.compare(readByte, FIRST_USER_DEFINED) >= 0) {
          return BinarySubtype.USER_DEFINED;
        } else {
          throw new AssertionError("Unrecognized binary type 0x" + UnsignedBytes.toString(readByte,
              16));
        }
      }
    }
  }

  protected static EnumSet<Options> parseRegexOptions(String optionsStr) {
    EnumSet<Options> result = EnumSet.noneOf(Options.class);

    if (optionsStr.isEmpty()) {
      return result;
    }

    int i = 0;
    if (optionsStr.charAt(i) == 'i') {
      result.add(Options.CASE_INSENSITIVE);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }
    if (optionsStr.charAt(i) == 'l') {
      result.add(Options.LOCALE_DEPENDENT);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }
    if (optionsStr.charAt(i) == 'm') {
      result.add(Options.MULTILINE_MATCHING);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }
    if (optionsStr.charAt(i) == 's') {
      result.add(Options.DOTALL_MODE);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }
    if (optionsStr.charAt(i) == 'u') {
      result.add(Options.UNICODE);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }
    if (optionsStr.charAt(i) == 'x') {
      result.add(Options.VERBOSE_MODE);

      i++;
      if (i >= optionsStr.length()) {
        return result;
      }
    }

    assert i < optionsStr.length();
    LOGGER.warn("Unexpected regex options '{}'", optionsStr.substring(i));

    return result;
  }

  private static class FromMongoFunction implements
      Function<BsonDocument, com.eightkdata.mongowp.bson.BsonDocument> {

    @Override
    public com.eightkdata.mongowp.bson.BsonDocument apply(@Nonnull BsonDocument t) {
      return translate(t);
    }
  }

  private static class ToMongoFunction implements
      Function<com.eightkdata.mongowp.bson.BsonDocument, BsonDocument> {

    @Override
    public BsonDocument apply(@Nonnull com.eightkdata.mongowp.bson.BsonDocument t) {
      try {
        return translate(t);
      } catch (IOException ex) {
        throw new RuntimeException("Unexpected IO exception", ex);
      }
    }

  }
}
