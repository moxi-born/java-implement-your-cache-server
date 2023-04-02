package com.moxib.exception;

public class MemberNotFoundException extends Exception {
  public MemberNotFoundException() {
  }

  public MemberNotFoundException(final Throwable rootCause) {
    super(rootCause);
  }
}
