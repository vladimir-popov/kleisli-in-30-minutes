package ru.dokwork.example.sync.tx

import ru.dokwork.example.Account

trait AccountRepo[Transaction] {
  def getById(id: Long, tx: Transaction): Account
  def update(account: Account, tx: Transaction): Boolean
}
