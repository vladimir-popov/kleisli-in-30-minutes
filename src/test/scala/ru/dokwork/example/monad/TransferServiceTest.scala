package ru.dokwork.example.monad

import cats.Id
import cats.implicits._
import org.scalatest.FunSuite
import ru.dokwork.example.Account

import scala.collection.mutable
import scala.util.Try

class TransferServiceTest extends FunSuite {

  test("without transaction") {
    // given:
    val accFrom = Account(1, 100)
    val accTo = Account(2, 50)
    val v = 20
    val mockRepo = new AccountRepo[Id] {
      val updateInvocations = new mutable.Queue[Account]()
      override def getById(id: Long) = if (id == 1) accFrom else accTo

      override def update(acc: Account) = {
        updateInvocations.enqueue(acc); true
      }
    }
    val service = new TransferService(mockRepo)

    // when:
    val result: Boolean = service.transfer(accFrom.id, accTo.id, v)

    // then:
    assert(mockRepo.updateInvocations.dequeue() == Account(1, 80))
    assert(mockRepo.updateInvocations.dequeue() == Account(2, 70))
  }

  test("with transaction") {
    // given:
    type Tx = mutable.Map[Long, Account]
    val accFrom = Account(1, 100)
    val accTo = Account(2, 50)
    val value = 20
    val persistence = mutable.Map[Long, Account](1L → accFrom, 2L → accTo)
    val mockRepo = new AccountRepoTx[Try, Tx] {
      override def getById(id: Long, tx: Tx) = Try {
        tx(id)
      }
      override def update(acc: Account, tx: Tx) = Try {
        tx.update(acc.id, acc)
        true
      }
    }
    val txManager = new TransactionManager[Try, Tx] {
      override def createTx(): Try[Tx] = Try {
        mutable.Map[Long, Account]()
      }
      override def begin(transaction: Tx): Try[Unit] = Try {
        transaction ++= persistence
      }
      override def commit(transaction: Tx): Try[Unit] = Try {
        persistence ++= transaction
      }
      override def rollback(transaction: Tx): Try[Unit] = Try {
        transaction.clear()
      }
    }
    val service = new TransferService(new AccountRepoKleisli(mockRepo))

    // when:
    val result: Try[Boolean] = txManager.withTx(
      service.transfer(accFrom.id, accTo.id, value).run
    )
    // then:
    assert(persistence(1L) == Account(1, 80))
    assert(persistence(2L) == Account(2, 70))
  }
}
