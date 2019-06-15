package com.scaledinference.utils

object OptionUtils {
  implicit class ToOption[T](value: T) extends AnyRef {
    def toOption: Option[T] = if (value == null ) Option.empty[T] else Some(value)
  }

  implicit class ToOptionString(value: String) extends AnyRef {
    def toOptionString: Option[String] = if (value == null || value == "") Option.empty[String] else Some(value)
  }
}
