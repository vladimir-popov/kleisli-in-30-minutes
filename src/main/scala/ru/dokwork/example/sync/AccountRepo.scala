package ru.dokwork.example.sync
import ru.dokwork.example.Account

trait AccountRepo {
  def getById(id: Long): Account
  def update(account: Account): Boolean
}
