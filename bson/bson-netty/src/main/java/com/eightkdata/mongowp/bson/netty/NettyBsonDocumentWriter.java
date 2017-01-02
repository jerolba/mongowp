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

package com.eightkdata.mongowp.bson.netty;

import com.eightkdata.mongowp.bson.BsonArray;
import com.eightkdata.mongowp.bson.BsonBinary;
import com.eightkdata.mongowp.bson.BsonBoolean;
import com.eightkdata.mongowp.bson.BsonDateTime;
import com.eightkdata.mongowp.bson.BsonDbPointer;
import com.eightkdata.mongowp.bson.BsonDecimal128;
import com.eightkdata.mongowp.bson.BsonDeprecated;
import com.eightkdata.mongowp.bson.BsonDocument;
import com.eightkdata.mongowp.bson.BsonDocument.Entry;
import com.eightkdata.mongowp.bson.BsonDouble;
import com.eightkdata.mongowp.bson.BsonInt32;
import com.eightkdata.mongowp.bson.BsonInt64;
import com.eightkdata.mongowp.bson.BsonJavaScript;
import com.eightkdata.mongowp.bson.BsonJavaScriptWithScope;
import com.eightkdata.mongowp.bson.BsonMax;
import com.eightkdata.mongowp.bson.BsonMin;
import com.eightkdata.mongowp.bson.BsonNull;
import com.eightkdata.mongowp.bson.BsonObjectId;
import com.eightkdata.mongowp.bson.BsonRegex;
import com.eightkdata.mongowp.bson.BsonString;
import com.eightkdata.mongowp.bson.BsonTimestamp;
import com.eightkdata.mongowp.bson.BsonUndefined;
import com.eightkdata.mongowp.bson.BsonValue;
import com.eightkdata.mongowp.bson.BsonValueVisitor;
import com.eightkdata.mongowp.bson.utils.NonIoByteSource;
import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedInteger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class NettyBsonDocumentWriter {

  private static final WriterBsonValueVisitor VISITOR = new WriterBsonValueVisitor();

  public void writeInto(ByteBuf byteBuf, BsonDocument doc) {
    doc.accept(VISITOR, byteBuf);
  }

  private static class WriterBsonValueVisitor implements BsonValueVisitor<Void, ByteBuf> {

    void writeCString(ByteBuf buf, String str) {
      buf.writeBytes(str.getBytes(Charsets.UTF_8));
      buf.writeByte(0x00);
    }

    void writeString(ByteBuf buf, String str) {
      byte[] bytes = str.getBytes(Charsets.UTF_8);
      int length = bytes.length + 1;

      buf.writeInt(length).writeBytes(bytes).writeByte(0x00);
    }

    @Override
    public Void visit(BsonArray value, ByteBuf arg) {
      final int docStart = arg.writerIndex();
      arg.writeInt(0); // reserve space for doc size

      int i = 0;
      for (BsonValue<?> child : value) {
        try {
          arg.writeByte(ParsingTools.getByte(child.getType()));
        } catch (NettyBsonReaderException ex) {
          throw new AssertionError(ex);
        }
        writeCString(arg, Integer.toString(i));

        child.accept(this, arg);
        i++;
      }

      arg.writeByte(0x00);

      int docEnd = arg.writerIndex();

      arg.writerIndex(docStart).writeInt(docEnd - docStart).writerIndex(docEnd);

      return null;
    }

    @Override
    public Void visit(BsonBinary value, ByteBuf arg) {
      NonIoByteSource byteSource = value.getByteSource();

      UnsignedInteger unsignedSize;
      unsignedSize = UnsignedInteger.valueOf(byteSource.size());

      arg.writeInt(unsignedSize.intValue()).writeByte(value.getNumericSubType());

      try (OutputStream os = new ByteBufOutputStream(arg)) {
        value.getByteSource().copyTo(os);
      } catch (IOException ex) {
        throw new AssertionError("Unexpected IOException", ex);
      }
      return null;
    }

    @Override
    public Void visit(BsonDbPointer value, ByteBuf arg) {
      writeString(arg, value.getNamespace());
      value.getId().accept(this, arg);

      return null;
    }

    @Override
    public Void visit(BsonDateTime value, ByteBuf arg) {
      arg.writeLong(value.getMillisFromUnix());

      return null;
    }

    @Override
    public Void visit(BsonDocument value, ByteBuf arg) {
      final int docStart = arg.writerIndex();
      arg.writeInt(0); // reserve space for doc size

      for (Entry<?> entry : value) {
        BsonValue<?> child = entry.getValue();
        try {
          arg.writeByte(ParsingTools.getByte(child.getType()));
        } catch (NettyBsonReaderException ex) {
          throw new AssertionError(ex);
        }
        writeCString(arg, entry.getKey());

        child.accept(this, arg);
      }

      arg.writeByte(0x00);

      int docEnd = arg.writerIndex();

      arg.writerIndex(docStart).writeInt(docEnd - docStart).writerIndex(docEnd);

      return null;
    }

    @Override
    public Void visit(BsonDouble value, ByteBuf arg) {
      arg.writeDouble(value.doubleValue());

      return null;
    }

    @Override
    public Void visit(BsonInt32 value, ByteBuf arg) {
      arg.writeInt(value.intValue());

      return null;
    }

    @Override
    public Void visit(BsonInt64 value, ByteBuf arg) {
      arg.writeLong(value.longValue());

      return null;
    }
    
    @Override
    public Void visit(BsonDecimal128 value, ByteBuf arg) {
      arg.writeFloat(value.floatValue());

      return null;
    }

    @Override
    public Void visit(BsonBoolean value, ByteBuf arg) {
      if (value.getPrimitiveValue()) {
        arg.writeByte(0x01);
      } else {
        arg.writeByte(0x00);
      }
      return null;
    }

    @Override
    public Void visit(BsonJavaScript value, ByteBuf arg) {
      writeString(arg, value.getValue());

      return null;
    }

    @Override
    public Void visit(BsonJavaScriptWithScope value, ByteBuf arg) {
      final int codeWsStart = arg.writerIndex();
      arg.writeInt(0); // reserve space for code_w_s size

      writeString(arg, value.getJavaScript());

      value.getScope().accept(VISITOR, arg);

      final int codeWsEnds = arg.writerIndex();

      arg.writerIndex(codeWsStart).writeInt(codeWsEnds - codeWsStart).writerIndex(codeWsEnds);

      return null;
    }

    @Override
    public Void visit(BsonMax value, ByteBuf arg) {
      return null;
    }

    @Override
    public Void visit(BsonMin value, ByteBuf arg) {
      return null;
    }

    @Override
    public Void visit(BsonNull value, ByteBuf arg) {
      return null;
    }

    @Override
    public Void visit(BsonObjectId value, ByteBuf arg) {
      arg.writeBytes(value.toByteArray());

      return null;
    }

    @Override
    public Void visit(BsonRegex value, ByteBuf arg) {
      writeCString(arg, value.getPattern());
      writeCString(arg, value.getOptionsAsText());

      return null;
    }

    @Override
    public Void visit(BsonString value, ByteBuf arg) {
      writeString(arg, value.getValue());

      return null;
    }

    @Override
    public Void visit(BsonUndefined value, ByteBuf arg) {
      return null;
    }

    @Override
    public Void visit(BsonTimestamp value, ByteBuf arg) {
      arg.writeInt(value.getOrdinal()).writeInt(value.getSecondsSinceEpoch());

      return null;
    }

    @Override
    public Void visit(BsonDeprecated value, ByteBuf arg) {
      writeString(arg, value.getValue());

      return null;
    }

  }
}
