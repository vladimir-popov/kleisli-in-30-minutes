package example

import cats.Monad
import cats.implicits._

class TransferService[M[_]](repo: AccountRepo[M]) {

  // Обратите внимание на то, что реализация описывает только правила бизнес логики,
  // при этом нюансы реализации (транзакция) никак не фигурирую здесь.
  def transfer(fromId: Long, toId: Long, value: BigDecimal)(implicit M: Monad[M]): M[Boolean] = {
    for {
      fromAccount ← repo.getById(fromId)
      updatedFrom = fromAccount.copy(balance = fromAccount.balance - value)
      toAccount ← repo.getById(toId)
      updatedTo = toAccount.copy(balance = toAccount.balance + value)
      u1 ← repo.update(updatedFrom)
      u2 ← repo.update(updatedTo)
    } yield u1 && u2
  }
}
