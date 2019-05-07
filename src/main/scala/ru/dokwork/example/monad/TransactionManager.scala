package ru.dokwork.example.monad

import cats.MonadError
import cats.implicits._

import scala.util.control.NonFatal

trait TransactionManager[M[_], Transaction] {

  def createTx(): M[Transaction]

  def begin(transaction: Transaction): M[Unit]

  def commit(transaction: Transaction): M[Unit]

  def rollback(transaction: Transaction): M[Unit]

  def withTx[T](f: Transaction ⇒ M[T])(implicit M: MonadError[M, Throwable]): M[T] =
    for {
      tx ← createTx()
      _ ← begin(tx)
      result ← f(tx).flatTap(_ ⇒ commit(tx)).onError {
        case NonFatal(e) ⇒ rollback(tx).flatMap(_ ⇒ M.raiseError(e))
      }
    } yield result
}
