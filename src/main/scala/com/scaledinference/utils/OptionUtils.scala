package com.scaledinference.utils

object OptionUtils {
  implicit class ToOption[T](value: T) extends AnyRef {
    def toOption: Option[T] = if (value == null) Option.empty[T] else Some(value)
  }
}
