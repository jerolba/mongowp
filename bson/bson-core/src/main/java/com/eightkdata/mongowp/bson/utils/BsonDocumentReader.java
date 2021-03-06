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

package com.eightkdata.mongowp.bson.utils;

import com.eightkdata.mongowp.bson.BsonDocument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that reads documents from a <em>source</em>.
 *
 * @param <SourceT> the class of the source from this reader will read the documents
 */
public interface BsonDocumentReader<SourceT> {

  /**
   * Reads a document from the input source.
   *
   * <p>
   * A heap allocation can be specified. The document returned will comply that allocation or one
   * more premisive than that if the reader doesn't support that heap allocation.
   *
   * <p>
   * {@link AllocationType#HEAP} must be always supported.
   */
  @Nonnull
  public BsonDocument readDocument(AllocationType allocationType, SourceT source)
      throws BsonDocumentReaderException;

  public static enum AllocationType {
    /**
     * Returns documents that are totally stored on the heap.
     */
    HEAP(null),
    /**
     * Returns documents whose metadata is on the heap but heavy values reside on the underlaying
     * {@link ByteBuf}.
     */
    OFFHEAP_VALUES(HEAP),
    /**
     * Returns light documents whose data and metadata reside on the underlaying {@link ByteBuf}
     * until the entry is accesed.
     */
    OFFHEAP(OFFHEAP_VALUES);

    private final AllocationType lessRestrictive;

    private AllocationType(AllocationType previous) {
      this.lessRestrictive = previous;
    }

    @Nullable
    public AllocationType getLessRestrictive() {
      return lessRestrictive;
    }
  }
}
