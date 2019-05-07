package ru.dokwork.example.monad

import ru.dokwork.example.Account

/**
  * Обобщенный интерфейс репозитория.
  */
trait AccountRepo[M[_]] {
  // Обратите внимание на то, что нюансы реализации (транзакции)
  // не фигурируют в интерфейсе.

  def getById(id: Long): M[Account]
  def update(account: Account): M[Boolean]
}
