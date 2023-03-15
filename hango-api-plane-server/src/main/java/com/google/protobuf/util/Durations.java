package com.google.protobuf.util;

import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import com.google.protobuf.Duration;

import java.text.ParseException;
import java.util.Comparator;

public final class Durations {
  static final long DURATION_SECONDS_MIN = -315576000000L;
  static final long DURATION_SECONDS_MAX = 315576000000L;
  public static final Duration MIN_VALUE = Duration.newBuilder().setSeconds(-315576000000L).setNanos(-999999999).build();
  public static final Duration MAX_VALUE = Duration.newBuilder().setSeconds(315576000000L).setNanos(999999999).build();
  private static final Comparator<Duration> COMPARATOR = new Comparator<Duration>() {
    public int compare(Duration d1, Duration d2) {
      Durations.checkValid(d1);
      Durations.checkValid(d2);
      int secDiff = Long.compare(d1.getSeconds(), d2.getSeconds());
      return secDiff != 0 ? secDiff : Integer.compare(d1.getNanos(), d2.getNanos());
    }
  };

  private Durations() {
  }

  public static Comparator<Duration> comparator() {
    return COMPARATOR;
  }

  public static int compare(Duration x, Duration y) {
    return COMPARATOR.compare(x, y);
  }

  public static boolean isValid(Duration duration) {
    return isValid(duration.getSeconds(), duration.getNanos());
  }

  public static boolean isValid(long seconds, int nanos) {
    if (seconds >= -315576000000L && seconds <= 315576000000L) {
      if ((long) nanos >= -999999999L && (long) nanos < 1000000000L) {
        return seconds >= 0L && nanos >= 0 || seconds <= 0L && nanos <= 0;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public static Duration checkValid(Duration duration) {
    long seconds = duration.getSeconds();
    int nanos = duration.getNanos();
    if (!isValid(seconds, nanos)) {
      throw new IllegalArgumentException(String.format("Duration is not valid. See proto definition for valid values. Seconds (%s) must be in range [-315,576,000,000, +315,576,000,000]. Nanos (%s) must be in range [-999,999,999, +999,999,999]. Nanos must have the same sign as seconds", seconds, nanos));
    } else {
      return duration;
    }
  }

  public static String toString(Duration duration) {
    checkValid(duration);
    long seconds = duration.getSeconds();
    int nanos = duration.getNanos();
    StringBuilder result = new StringBuilder();
    if (seconds < 0L || nanos < 0) {
      result.append("-");
      seconds = -seconds;
      nanos = -nanos;
    }

    result.append(seconds);
    if (nanos != 0) {
      result.append(".");
      result.append(Timestamps.formatNanos(nanos));
    }

    result.append("s");
    return result.toString();
  }

  public static Duration parse(String value) throws ParseException {
    // 兼容ms
    if (value.endsWith("ms")) {
      return fromMillis(Long.parseLong(value.substring(0, value.length() - 2)));
    }
    if (!value.isEmpty() && value.charAt(value.length() - 1) == 's') {
      boolean negative = false;
      if (value.charAt(0) == '-') {
        negative = true;
        value = value.substring(1);
      }

      String secondValue = value.substring(0, value.length() - 1);
      String nanoValue = "";
      int pointPosition = secondValue.indexOf(46);
      if (pointPosition != -1) {
        nanoValue = secondValue.substring(pointPosition + 1);
        secondValue = secondValue.substring(0, pointPosition);
      }

      long seconds = Long.parseLong(secondValue);
      int nanos = nanoValue.isEmpty() ? 0 : Timestamps.parseNanos(nanoValue);
      if (seconds < 0L) {
        throw new ParseException("Invalid duration string: " + value, 0);
      } else {
        if (negative) {
          seconds = -seconds;
          nanos = -nanos;
        }

        try {
          return normalizedDuration(seconds, nanos);
        } catch (IllegalArgumentException var9) {
          throw new ParseException("Duration value is out of range.", 0);
        }
      }
    } else {
      throw new ParseException("Invalid duration string: " + value, 0);
    }
  }

  public static Duration fromSeconds(long seconds) {
    return normalizedDuration(seconds, 0);
  }

  public static long toSeconds(Duration duration) {
    return checkValid(duration).getSeconds();
  }

  public static Duration fromMillis(long milliseconds) {
    return normalizedDuration(milliseconds / 1000L, (int) (milliseconds % 1000L * 1000000L));
  }

  public static long toMillis(Duration duration) {
    checkValid(duration);
    return LongMath.checkedAdd(LongMath.checkedMultiply(duration.getSeconds(), 1000L), (long) duration.getNanos() / 1000000L);
  }

  public static Duration fromMicros(long microseconds) {
    return normalizedDuration(microseconds / 1000000L, (int) (microseconds % 1000000L * 1000L));
  }

  public static long toMicros(Duration duration) {
    checkValid(duration);
    return LongMath.checkedAdd(LongMath.checkedMultiply(duration.getSeconds(), 1000000L), (long) duration.getNanos() / 1000L);
  }

  public static Duration fromNanos(long nanoseconds) {
    return normalizedDuration(nanoseconds / 1000000000L, (int) (nanoseconds % 1000000000L));
  }

  public static long toNanos(Duration duration) {
    checkValid(duration);
    return LongMath.checkedAdd(LongMath.checkedMultiply(duration.getSeconds(), 1000000000L), (long) duration.getNanos());
  }

  public static Duration add(Duration d1, Duration d2) {
    checkValid(d1);
    checkValid(d2);
    return normalizedDuration(LongMath.checkedAdd(d1.getSeconds(), d2.getSeconds()), IntMath.checkedAdd(d1.getNanos(), d2.getNanos()));
  }

  public static Duration subtract(Duration d1, Duration d2) {
    checkValid(d1);
    checkValid(d2);
    return normalizedDuration(LongMath.checkedSubtract(d1.getSeconds(), d2.getSeconds()), IntMath.checkedSubtract(d1.getNanos(), d2.getNanos()));
  }

  static Duration normalizedDuration(long seconds, int nanos) {
    if ((long) nanos <= -1000000000L || (long) nanos >= 1000000000L) {
      seconds = LongMath.checkedAdd(seconds, (long) nanos / 1000000000L);
      nanos = (int) ((long) nanos % 1000000000L);
    }

    if (seconds > 0L && nanos < 0) {
      nanos = (int) ((long) nanos + 1000000000L);
      --seconds;
    }

    if (seconds < 0L && nanos > 0) {
      nanos = (int) ((long) nanos - 1000000000L);
      ++seconds;
    }

    Duration duration = Duration.newBuilder().setSeconds(seconds).setNanos(nanos).build();
    return checkValid(duration);
  }
}
