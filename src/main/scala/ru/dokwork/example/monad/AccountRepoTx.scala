package ru.dokwork.example.monad

import ru.dokwork.example.Account

/**
  * Интерфейс более частной реализации репозитория с поддержкой транзакций.
  */
trait AccountRepoTx[M[_], Transaction] {
  def getById(id: Long, tx: Transaction): M[Account]
  def update(account: Account, tx: Transaction): M[Boolean]
}
