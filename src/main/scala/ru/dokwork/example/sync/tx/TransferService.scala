package ru.dokwork.example.sync.tx

import java.sql.Connection

class TransferService(
    repo: AccountRepo[Connection],
    txManager: TransactionManager[Connection]
) {
  def transfer(fromId: Long, toId: Long, value: BigDecimal) = {
    txManager.withTx { tx ⇒
      val fromAccount = {
        val x = repo.getById(fromId, tx)
        x.copy(balance = x.balance - value)
      }

      val toAccount = {
        val x = repo.getById(toId, tx)
        x.copy(balance = x.balance + value)
      }

      repo.update(fromAccount, tx) &&
      repo.update(toAccount, tx)
    }
  }
}
