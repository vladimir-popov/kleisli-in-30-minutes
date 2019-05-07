package ru.dokwork.example.monad

import cats.data.Kleisli
import ru.dokwork.example.Account

import scala.concurrent.Future

// ({ type G[T] = Kleisli[M, Transaction, T] })#G - способ привести тип, вида F[_, _, _] к виду G[_]
class AccountRepoKleisli[M[_], Transaction](repo: AccountRepoTx[M, Transaction])
    extends AccountRepo[({ type G[T] = Kleisli[M, Transaction, T] })#G] {

  override def getById(id: Long): Kleisli[M, Transaction, Account] =
    Kleisli(tx ⇒ repo.getById(id, tx))


  override def update(account: Account): Kleisli[M, Transaction, Boolean] =
    Kleisli(tx ⇒ repo.update(account, tx))
}