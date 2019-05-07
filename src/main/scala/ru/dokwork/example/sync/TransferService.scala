package ru.dokwork.example.sync

class TransferService(repo: AccountRepo) {

  def transfer(fromId: Long, toId: Long, value: BigDecimal): Boolean = {
    // получаем аккаунт и списываем средства
    val fromAccount = {
      val x = repo.getById(fromId)
      x.copy(balance = x.balance - value)
    }

    // получаем второй аккаунт и начисляем средства
    val toAccount = {
      val x = repo.getById(toId)
      x.copy(balance = x.balance + value)
    }

    // сохраняем изменения
    repo.update(fromAccount) && repo.update(toAccount)
  }
}
