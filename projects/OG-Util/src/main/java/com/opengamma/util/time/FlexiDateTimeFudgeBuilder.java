/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.Calendrical;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code FlexiDateTime}.
 */
@FudgeBuilderFor(FlexiDateTime.class)
public final class FlexiDateTimeFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FlexiDateTime> {

  /** Field name. */
  public static final String DATETIME_FIELD_NAME = "datetime";
  /** Field name. */
  public static final String ZONE_FIELD_NAME = "zone";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FlexiDateTime object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final FlexiDateTime object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final FlexiDateTime object, final MutableFudgeMsg msg) {
    Calendrical best = object.toBest();
    best = (best instanceof ZonedDateTime ? ((ZonedDateTime) best).toOffsetDateTime() : best);
    addToMessage(msg, DATETIME_FIELD_NAME, best);
    if (object.getZone() != null && object.getZone().isFixed() == false) {
      addToMessage(msg, ZONE_FIELD_NAME, object.getZone());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FlexiDateTime buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static FlexiDateTime fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final TimeZone zone = msg.getValue(TimeZone.class, ZONE_FIELD_NAME);
    final Object obj = msg.getValue(DATETIME_FIELD_NAME);
    if (obj instanceof FudgeDateTime) {
      FudgeDateTime fudge = (FudgeDateTime) obj;
      if (fudge.getTime().hasTimezoneOffset()) {
        OffsetDateTime odt = fudge.toOffsetDateTime();
        if (zone != null) {
          return FlexiDateTime.of(odt.atZoneSameInstant(zone));
        }
        return FlexiDateTime.of(odt);
      } else {
        return FlexiDateTime.of(fudge.toLocalDateTime());
      }
    } else if (obj instanceof FudgeDate) {
      FudgeDate fudge = (FudgeDate) obj;
      return FlexiDateTime.of(fudge.toLocalDate());
    } else if (obj instanceof OffsetDateTime) {
      OffsetDateTime odt = (OffsetDateTime) obj;
      if (zone != null) {
        return FlexiDateTime.of(odt.atZoneSameInstant(zone));
      }
      return FlexiDateTime.of(odt);
    } else if (obj instanceof LocalDateTime) {
      return FlexiDateTime.of((LocalDateTime) obj);
    } else if (obj instanceof LocalDate) {
      return FlexiDateTime.of((LocalDate) obj);
    } else {
      throw new IllegalStateException("Fudge message did not contain a valid date-time");
    }
  }

}
