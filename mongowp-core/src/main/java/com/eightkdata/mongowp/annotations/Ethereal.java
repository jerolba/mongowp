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

package com.eightkdata.mongowp.annotations;

import com.eightkdata.mongowp.bson.BsonDocument;
import com.eightkdata.mongowp.messages.request.BsonContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@linkplain BsonDocument document} or documents annotated with this class can depend on a
 * {@link BsonContext}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Ethereal {

  /**
   * A text that identifies the object on which the annotated documents depend on.
   *
   * That object is usually a {@link BsonContext} or a {@link AutoCloseable} object that wraps a
   * BsonContext.
   *
   * @return
   */
  String value();
}
