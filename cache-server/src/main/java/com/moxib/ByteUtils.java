package com.moxib;

import java.io.Serializable;
import java.util.Comparator;

public class ByteUtils {

  private static final ByteArrayComparator BYTES_LEXICO_COMPARATOR = new LexicographicByteArrayComparator();

  public static ByteArrayComparator getDefaultByteArrayComparator() {
    return BYTES_LEXICO_COMPARATOR;
  }

  public interface ByteArrayComparator extends Comparator<byte[]>, Serializable {

    int compare(final byte[] buffer1, final int offset1, final int length1, final byte[] buffer2,
                final int offset2, final int length2);
  }

  private static class LexicographicByteArrayComparator implements ByteArrayComparator {

    private static final long serialVersionUID = -8623342242397267864L;

    @Override
    public int compare(final byte[] buffer1, final byte[] buffer2) {
      return compare(buffer1, 0, buffer1.length, buffer2, 0, buffer2.length);
    }

    @Override
    public int compare(final byte[] buffer1, final int offset1, final int length1, final byte[] buffer2,
                       final int offset2, final int length2) {
      // short circuit equal case
      if (buffer1 == buffer2 && offset1 == offset2 && length1 == length2) {
        return 0;
      }
      // similar to Arrays.compare() but considers offset and length
      final int end1 = offset1 + length1;
      final int end2 = offset2 + length2;
      for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
        int a = buffer1[i] & 0xff;
        int b = buffer2[j] & 0xff;
        if (a != b) {
          return a - b;
        }
      }
      return length1 - length2;
    }
  }
}
