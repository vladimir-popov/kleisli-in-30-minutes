package ru.dokwork.example.sync.tx

trait TransactionManager[Transaction] {
  def createTx(): Transaction

  def begin(transaction: Transaction)

  def commit(transaction: Transaction)

  def rollback(transaction: Transaction)

  def withTx[T](f: Transaction ⇒ T): T = {
    val tx = this.createTx()
    try {
      begin(tx)
      val result = f(tx)
      commit(tx)
      result
    } catch {
      case e: Exception ⇒
        rollback(tx)
        throw e
    }
  }
}
